package au.com.vaadinutils.crud;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent.EntitiesAddedEvent;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent.EntitiesRemovedEvent;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent.EntitiesUpdatedEvent;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainer.ProviderChangedEvent;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import au.com.vaadinutils.crud.events.CrudEventDistributer;
import au.com.vaadinutils.crud.events.CrudEventType;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.errorHandling.ErrorWindow;

/**
 * child crud does not support nesting.
 *
 * @author rsutton
 *
 * @param <P>
 * @param <E>
 */
public abstract class ChildCrudView<P extends CrudEntity, E extends ChildCrudEntity> extends BaseCrudView<E>
		implements ChildCrudListener<P>
{

	private static final long serialVersionUID = -7756584349283089830L;
	private transient Logger loggerChildCrud = LogManager.getLogger(ChildCrudView.class);
	private String parentKey;
	protected String childKey;
	public Object parentId;
	protected P currentParent;
	protected Filter parentFilter;
	protected boolean dirty = false;
	final private Class<P> parentType;
	public ParentCrud<P> parentCrud;
	private ChildCrudEventHandler<E> eventHandler = getNullEventHandler();
	private Class<E> childType;

	/**
	 *
	 * @param parentKey
	 *            - this will usually be the primary key of the parent table
	 * @param childKey
	 *            - this will be the foreign key in the child table
	 */
	public ChildCrudView(ParentCrud<P> parent, Class<P> parentType, Class<E> childType,
			SingularAttribute<? extends CrudEntity, ? extends Object> parentKey,
			SingularAttribute<? extends CrudEntity, ? extends Object> childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey.getName();
		this.parentType = parentType;
		this.parentCrud = parent;
		this.childType = childType;

		// setMargin(true);

	}

	public ChildCrudView(ParentCrud<P> parent, Class<P> parentType, Class<E> childType,
			SingularAttribute<? extends CrudEntity, ? extends Object> parentKey, String childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey;
		this.parentType = parentType;
		this.parentCrud = parent;
		this.childType = childType;
		// setMargin(true);

	}

	// public ChildCrudView(final BaseCrudView<P> parentCrud, Class<P>
	// parentType, Class<E> childType,
	// SingularAttribute<? extends CrudEntity, ? extends Object> parentKey,
	// SingularAttribute<? extends CrudEntity, ? extends Object> childKey)
	// {
	// super(CrudDisplayMode.VERTICAL);
	// this.parentKey = parentKey.getName();
	// this.childKey = childKey.getName();
	// this.parentType = parentType;
	// this.childType = childType;
	// this.parentCrud = new ParentCrud<P>(){
	//
	// @Override
	// public EntityItem<P> getContainerItem(Long id)
	// {
	// return parentCrud.getContainer().getItem(id);
	// }
	//
	// @Override
	// public void fieldGroupIsDirty(boolean b)
	// {
	// parentCrud.fieldGroupIsDirty(b);
	//
	// }
	//
	// @Override
	// public P getCurrent()
	// {
	// return parentCrud.getCurrent();
	// }
	//
	// @Override
	// public boolean isDirty()
	// {
	// return parentCrud.isDirty();
	// }
	//
	// @Override
	// public void reloadDataFromDB()
	// {
	// parentCrud.reloadDataFromDB();
	//
	// }
	//
	// @Override
	// public void save()
	// {
	// parentCrud.save();
	//
	// }
	//
	// @Override
	// public void setSplitPosition(float pos)
	// {
	// parentCrud.setSplitPosition(pos);
	//
	// }};
	//
	// }

	@Override
	protected void init(Class<E> entityClass, JPAContainer<E> container, HeadingPropertySet headings)
	{

		setBogusParentFilter();
		super.init(entityClass, container, headings);
		// ensure auto commit is off, so that child updates don't go to the db
		// until the parent saves
		container.setAutoCommit(false);
		// container.setBuffered(true);

		// child cruds dont have save/cancel buttons
		if (buttonLayout != null)
		{
			rightLayout.removeComponent(buttonLayout);
		}

	}

	/**
	 * Load the page with a bogus parent filter to prevent possible large
	 * queries from being executed before a parent row is selected
	 */
	private void setBogusParentFilter()
	{
		P tmp;
		try
		{
			tmp = parentType.getDeclaredConstructor().newInstance();
			tmp.setId(-1L);
			parentFilter = new Compare.Equal(childKey, tmp);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e)
		{
			loggerChildCrud.warn("Failed to instance " + parentType + " to create bogus parent filter");

		}
	}

	/**
	 * this method is invoked when the parent saves, signalling the children
	 * that they too should save.
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void committed(P newParentId) throws Exception
	{
		Long selectedIdBeforeSave = null;
		String currentGuid = null;
		if (getCurrent() != null)
		{
			selectedIdBeforeSave = getCurrent().getId();
			currentGuid = getCurrent().getGuid();
		}
		resetFiltersWithoutChangeEvents();
		saveEditsToTemp();
		int numberOfChildren = 0;
		for (Object id : container.getItemIds())
		{

			numberOfChildren++;
			EntityItem<E> item = container.getItem(id);
			if (item != null)
			{

				EntityItemProperty reference = item.getItemProperty(childKey);

				if (reference == null)
				{
					loggerChildCrud.error(
							"Child key " + childKey + " doesn't exist in the container " + container.getEntityClass());
				}
				if (reference == null || reference.getValue() == null)
				{
					try
					{

						// TODO: this looks like the spot to allow a ManyToMany
						// relationship to be updated,
						// need to add a hook for that here (I think, may be
						// problems with the parent when it's also a new parent)
						if (item.getItemProperty(childKey).getType().isAssignableFrom(newParentId.getClass()))
						{
							item.getItemProperty(childKey).setValue(newParentId);
						}
						else
						{
							loggerChildCrud.warn(
									"Child key type is not the same as the Parent type, if it's an ID thats probably ok?");
							// special handling when the child key is an
							// id(Long)
							// rather than an entity.
							item.getItemProperty(childKey).setValue(translateParentId(newParentId.getId()));
						}
						// item.getItemProperty(childKey).setValue(newParentId);

					}
					catch (Exception e)
					{
						loggerChildCrud.error(e, e);
					}

				}
				extendedChildCommitProcessing(newParentId, item);

			}
			else
			{
				loggerChildCrud.error("Missing item {} from container", id);
			}

		}
		// container.commit();
		loggerChildCrud.info("Committing for " + this.getClass());
		commitContainerWithHooks();

		// on a new parent, the parent id changes and the container becomes
		// empty. so reset the parent filter and refresh the container
		createParentFilter(parentCrud.getContainerItem(newParentId.getId()));
		resetFiltersWithoutChangeEvents();

		// container.discard();
		container.refresh();

		int changeInItems = numberOfChildren - container.getItemIds().size();
		if (changeInItems != 0)
		{
			// This is usually caused by a filter that eliminates a newly added
			// child. An example of this is where a child crud is associated via
			// a ManyToMany and the ManyToMany relationship is not yet updated
			// and the filter therefore eliminates the new child. - see the
			// above TODO.
			// Another example is when resetFilter has been overridden and the
			// resulting filters eliminate the new child

			// it is also possible for the number of items to change if there
			// are multiple users editing simultaneously

			if (ignoreIncorrectNumberOfItems())
			{
				loggerChildCrud.warn(changeInItems
						+ ", The number of items in the container is not the same as it was before the refresh. ");
			}
			else
			{
				Notification.show(
						"Some of the items may not have been saved, or another user may have been editing the same record at the same time.\n\n"
								+ "You should refresh the browser page (press F5) and check that your changes were correctly saved.",
						Type.ERROR_MESSAGE);
				loggerChildCrud.error(changeInItems
						+ ", The number of items in the container is not the same as it was before the refresh. "
						+ this.getClass().getSimpleName() + " " + numberOfChildren + " != "
						+ container.getItemIds().size());
			}
		}
		associateChildren(newParentId);
		dirty = false;
		entityTable.select(null);
		triggerFilter();
		if (selectedIdBeforeSave != null && selectedIdBeforeSave > 0)
		{
			entityTable.select(selectedIdBeforeSave);
		}
		else
		{
			if (getGuidAttribute() != null && currentGuid != null)
			{
				E entity = JpaBaseDao.getGenericDao(childType).findOneByAttribute(getGuidAttribute(), currentGuid);
				if (entity != null)
				{
					entityTable.select(entity.getId());
				}
				else
				{
					loggerChildCrud.warn(
							"Unable to locate newly created child entity, will not be able to select it for the user.");
				}
			}
		}

	}

	/**
	 * override this method and return ture if this crud is for a highly
	 * contended table.
	 * 
	 * @return
	 */
	protected boolean ignoreIncorrectNumberOfItems()
	{

		return false;
	}

	/**
	 * return the singluarAttribute for the guid field of the child entity
	 *
	 * @return
	 */
	abstract public SingularAttribute<? super E, String> getGuidAttribute();

	/**
	 * commits the container and retrieves the new recordid
	 *
	 * we have to hook the ItemSetChangeListener to be able to get the database
	 * id of a new record.
	 */
	private void commitContainerWithHooks()
	{

		// call back to collect the id of the new record when the container
		// fires the ItemSetChangeEvent
		ItemSetChangeListener tmp = new ItemSetChangeListener()
		{

			private static final long serialVersionUID = -4893881323343394274L;

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
				if (event instanceof ProviderChangedEvent)
				{
					@SuppressWarnings("rawtypes")
					ProviderChangedEvent pce = (ProviderChangedEvent) event;
					@SuppressWarnings("unchecked")
					EntityProviderChangeEvent<E> changeEvent = pce.getChangeEvent();

					if (changeEvent instanceof EntitiesAddedEvent)
					{
						Collection<E> affectedEntities = changeEvent.getAffectedEntities();
						eventHandler.entitiesAdded(affectedEntities);
					}

					if (changeEvent instanceof EntitiesUpdatedEvent)
					{
						Collection<E> affectedEntities = changeEvent.getAffectedEntities();
						eventHandler.entitiesUpdated(affectedEntities);
					}

					if (changeEvent instanceof EntitiesRemovedEvent)
					{
						Collection<E> affectedEntitys = changeEvent.getAffectedEntities();
						eventHandler.entitiesDeleted(affectedEntitys);
					}
				}
			}
		};

		final LinkedList<ItemSetChangeListener> listeners = new LinkedList<>(container.getItemSetChangeListeners());
		try
		{
			// get existing listeners and remove them
			for (ItemSetChangeListener listener : listeners)
			{
				container.removeItemSetChangeListener(listener);
			}
			// add the hook listener
			container.addItemSetChangeListener(tmp);
			// call commit
			container.commit();
		}
		catch (Exception e)
		{
			ErrorWindow.showErrorWindow(e);
		}
		finally
		{
			// detach the hook listener
			container.removeItemSetChangeListener(tmp);
			// restore the existing listeners
			for (ItemSetChangeListener listener : listeners)
			{
				container.addItemSetChangeListener(listener);
			}
		}

	}

	/**
	 * EventHandler is the preferred integration point for classes extending
	 * ChildCrudView as it makes them less tightly coupled.
	 *
	 * @param eventHandler
	 */
	public void setEventHandler(ChildCrudEventHandler<E> eventHandler)
	{
		this.eventHandler = eventHandler;
	}

	private void associateChildren(P newParent) throws Exception
	{
		P mParent = EntityManagerProvider.merge(newParent);
		for (Object id : container.getItemIds())
		{
			E child = EntityManagerProvider.merge(container.getItem(id).getEntity());
			associateChild(mParent, child);
			for (ChildCrudListener<E> childListener : getChildCrudListeners())
			{
				// allow child of child crud to commit
				childListener.committed(child);
			}
		}

	}

	abstract public void associateChild(P newParent, E child);

	/**
	 * @throws Exception
	 */
	protected void extendedChildCommitProcessing(P newParentId, EntityItem<E> item) throws Exception
	{

	}

	/**
	 * if a row is deleted in the childCrud, then it is dirty for the purposes
	 * of the parent crud
	 */
	@Override
	public void delete()
	{
		Object entityId = entityTable.getValue();
		preChildDelete(entityId);
		Object previousItemId = null;
		try
		{
			previousItemId = entityTable.prevItemId(entityId);
		}
		catch (Exception e)
		{
			loggerChildCrud.warn(e, e);
		}

		entityTable.removeItem(entityId);
		newEntity = null;
		searchField.setReadOnly(false);

		entityTable.select(null);
		entityTable.select(previousItemId);

		dirty = true;
		container.removeItem(entityId);
		JpaBaseDao<E, Long> dao = new JpaBaseDao<>(entityClass);
		E entity = dao.findById((Long) entityId);
		if (entity != null)
		{
			EntityManagerProvider.remove(entity);
		}
		CrudEventDistributer.publishEvent(this, CrudEventType.DELETE, entity);
		parentCrud.reloadDataFromDB();
		entityTable.select(null);

		postChildDelete(entityId);

	}

	/**
	 * Called just before a child entity is deleted so that a derived class can
	 * inject some logic just before the delete occurs.
	 *
	 * @param entityId
	 */
	protected void preChildDelete(Object entityId)
	{
	}

	/**
	 * Called just after a child entity is deleted so that a derived class can
	 * inject some logic just after the delete occurs.
	 *
	 * @param entityId
	 */
	protected void postChildDelete(Object entityId)
	{
	}

	@Override
	public void validateFieldz()
	{
		try
		{
			if (getCurrent() != null)
			{
				String fieldName = selectFirstErrorFieldAndShowTab(this.fieldGroup);
				if (!fieldGroup.isValid())
				{

					throw new InvalidValueException("Invalid Field: " + fieldName);

				}
			}
		}
		catch (InvalidValueException e)
		{
			throw e;
		}
	}

	/**
	 * Need to over-ride as the rules display of a child's new button a
	 * different to those of the parent.
	 */
	@Override
	protected void activateEditMode(boolean activate)
	{
		actionCombo.setEnabled(!activate);
		actionApplyButton.setEnabled(!activate);

		// for child new is always enabled unless explicitly disallowed
		boolean showNew = true;
		if (isDisallowNew())
		{
			showNew = false;
		}
		actionNewButton.setEnabled(showNew);
	}

	/**
	 * used to prevent cascading saves when new is clicked
	 */
	boolean inNew = false;

	@Override
	public void rowChanged(EntityItem<E> item)
	{
		if (preventRowChangeCascade == false && !inNew)
		{
			try
			{
				preventRowChangeCascade = true;
				if (isInitialised)
				{
					// without this conditional, we try to save fields which
					// don't have a datasource set and it will blowup
					saveEditsToTemp();
				}
				super.rowChanged(item);
				activateEditMode(false);
			}
			finally
			{
				preventRowChangeCascade = false;
			}
		}
		else
		{

			super.rowChanged(item);
		}

	}

	@Override
	public void newClicked()
	{

		try
		{
			inNew = true;
			E previousEntity = getCurrent();
			saveEditsToTemp();
			resetFiltersWithoutChangeEvents();
			triggerFilter();

			createNewEntity(previousEntity);

			// if we call the overridden version we loop indefinitely

			entityTable.select(null);
			ChildCrudView.super.rowChanged(newEntity);

			// Can't delete when you are adding a new record.
			// Use cancel instead.
			if (actionApplyButton.isVisible())
			{
				restoreDelete = true;
				activateEditMode(true);
				actionLayout.setVisible(true);
			}

			selectFirstFieldAndShowTab();

			postNew(newEntity);

			searchField.setReadOnly(true);

			rightLayout.setVisible(true);
		}
		catch (ConstraintViolationException e)
		{
			FormHelper.showConstraintViolation(e);
		}
		catch (InstantiationException e)
		{
			loggerChildCrud.error(e, e);
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			loggerChildCrud.error(e, e);
			throw new RuntimeException(e);
		}
		catch (Exception e)
		{
			loggerChildCrud.error(e, e);
			throw new RuntimeException(e);
		}
		finally
		{
			inNew = false;
		}

	}

	/**
	 * for child crud, save is implied when the row changes
	 */
	@Override
	public void save()
	{

	}

	@Override
	protected void invokeTopLevelCrudSave()
	{
		parentCrud.save();
	}

	/**
	 * used to prevent cascading save calls
	 */
	boolean saving = false;

	@Override
	public void saveEditsToTemp()
	{
		if (saving == false)
		{
			try
			{
				saving = true;
				// if a row is saved in the childCrud, then it is dirty for the
				// purposes of the parent crud
				if (fieldGroup.isModified() || areNonFieldGroupFieldsDirty())
				{
					dirty = true;
					commitFieldGroup();

					if (newEntity != null)
					{
						interceptSaveValues(newEntity);

						container.addEntity(newEntity.getEntity());
						// EntityItem<E> item = container.getItem(id);
						// container.commit();

						// fieldGroup.setItemDataSource(item);
						// entityTable.select(item.getItemId());
						// If we leave the save button active, clicking it again
						// duplicates the record
						// rightLayout.setVisible(false);
						CrudEventDistributer.publishEvent(this, CrudEventType.CREATE, newEntity.getEntity());
					}
					else
					{

						EntityItem<E> current = entityTable.getCurrent();
						if (current != null)
						{
							interceptSaveValues(current);
							// container.commit();
							CrudEventDistributer.publishEvent(this, CrudEventType.EDIT, current.getEntity());
						}
					}

				}
				else
				{
					if (getCurrent() != null)
					{
						loggerChildCrud.info("There are no dirty fields, not saving record {} {}",
								getCurrent().getClass().getSimpleName(), getCurrent());
					}
				}

				for (ChildCrudListener<E> child : getChildCrudListeners())
				{
					child.saveEditsToTemp();
				}
				// Notification.show("Changes Saved",
				// "Any changes you have made have been saved.",
				// Type.TRAY_NOTIFICATION);

			}
			catch (PersistenceException e)
			{
				loggerChildCrud.error(e, e);
				Notification.show(e.getClass().getSimpleName(), Type.ERROR_MESSAGE);
			}
			catch (ConstraintViolationException e)
			{
				loggerChildCrud.error(e, e);
				FormHelper.showConstraintViolation(e);
			}
			catch (InvalidValueException e)
			{
				handleInvalidValueException(e);
			}
			catch (CommitException e)
			{
				if (e.getCause() instanceof InvalidValueException)
				{
					handleInvalidValueException((InvalidValueException) e.getCause());
				}
				else
				{
					loggerChildCrud.error(e, e);
					Notification.show(e.getClass().getSimpleName(), Type.ERROR_MESSAGE);
				}
			}
			catch (Exception e)
			{
				loggerChildCrud.error(e, e);
				Notification.show(e.getClass().getSimpleName(), Type.ERROR_MESSAGE);
			}

			finally
			{
				if (newEntity != null)
				{
					newEntity = null;
					if (restoreDelete)
					{
						activateEditMode(false);
						restoreDelete = false;
					}
					searchField.setReadOnly(false);
				}
				splitPanel.showFirstComponent();
				saving = false;
			}
			// }
			// }
		}

	}

	/**
	 *
	 *
	 * When adding fields to a layout that need to be committed, but are not
	 * part of the field group, override this method to let the crud know that a
	 * commit should be performed.
	 *
	 * This is required because committing on a child crud is not explicit -no
	 * save button is available, the need to save is determined based on if the
	 * fields are dirty.
	 *
	 * @return
	 */
	protected boolean areNonFieldGroupFieldsDirty()
	{
		return false;
	}

	/**
	 * used to prevent cascading rowChange events
	 */
	boolean preventRowChangeCascade = false;
	private Collection<RowChangedListener<P>> parentRowChangeListeners = new LinkedList<>();

	private boolean isInitialised = false;

	@Override
	protected void initializeEntityTable()
	{
		// do nothing, this will be done when the parent selects the first row!
	}

	/**
	 * this method is called when the parent crud changes row, so we set filters
	 * so that this child only displays the associated rows
	 */
	@Override
	public void selectedParentRowChanged(EntityItem<P> item)
	{
		try
		{

			if (item != null && item.getEntity() != null)
			{
				loggerChildCrud.debug("Parent Row Changed {} {}", item.getEntity().getId(), item.getEntity().getName());
			}

			searchField.setValue("");
			clearAdvancedFilters();

			if (isInitialised)
			{
				saveEditsToTemp();
			}
			createParentFilter(item);
			currentParent = null;
			if (item != null)
			{
				currentParent = item.getEntity();
			}
			if (isInitialised)
			{
				fieldGroup.discard();
				container.discard();
			}
			dirty = false;

			Stopwatch timer = Stopwatch.createUnstarted();
			timer.start();
			resetFiltersWithoutChangeEvents();
			triggerFilter();
			if (!isInitialised)
			{
				entityTable.init(this.getClass().getSimpleName());

				// entityTable doesn't generate a row change event on this path,
				// so we'll simulate one!
				rowChanged(entityTable.getCurrent());

				isInitialised = true;
			}
			loggerChildCrud.debug("Child crud load {} took {}", this.getClass().getSimpleName(),
					timer.elapsed(TimeUnit.MILLISECONDS));

			Object id = entityTable.firstItemId();
			if (id != null)
			{
				entityTable.select(entityTable.firstItemId());
			}
			else
			{
				try
				{
					entityTable.select(null);
				}
				catch (Exception e)
				{
					loggerChildCrud.warn(e, e);
					// ignore this. if we don't do this the child continues
					// to
					// show data from the previously selected row

					// TODO: come up with a better solution
				}
			}
			searchField.setValue("");
			if (item == null)
			{
				notifyParentRowChangeListeners(null);
			}
			else
			{
				notifyParentRowChangeListeners(item.getEntity());
			}
		}
		catch (Exception e)
		{
			ErrorWindow.showErrorWindow(e);
		}

	}

	private void notifyParentRowChangeListeners(P entity)
	{
		for (RowChangedListener<P> listener : parentRowChangeListeners)
		{
			listener.rowChanged(entity);
		}

	}

	public void addParentRowChangedListener(RowChangedListener<P> rowChangedListener)
	{
		parentRowChangeListeners.add(rowChangedListener);

	}

	private void createParentFilter(EntityItem<P> item) throws InstantiationException, IllegalAccessException
	{
		parentFilter = new Compare.Equal(childKey, translateParentId(-1l));

		if (item != null)

		{
			EntityItemProperty key = item.getItemProperty(parentKey);
			Preconditions.checkNotNull(key, "parentKey " + parentKey + " doesn't exist in properties");
			parentId = key.getValue();
			if (parentId != null)
			{

				parentFilter = new Compare.Equal(childKey, translateParentId(parentId));

			}

		}
	}

	/**
	 * the id (Long) of the parent will need to be translated into an entity for
	 * the filtering. an implementing class must implement this method and
	 * return an instance of the Parent class (P) with it's id set (parentId2)
	 *
	 * @param parentId2
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	protected Object translateParentId(Object parentId2) throws InstantiationException, IllegalAccessException
	{

		Preconditions.checkNotNull(parentId2,
				"attempt to translate null parent id in " + entityClass.getCanonicalName());
		final P tmp;
		try
		{
			tmp = parentType.getDeclaredConstructor().newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		Preconditions.checkNotNull(tmp, "failed to create instance of " + entityClass.getCanonicalName());
		tmp.setId((Long) parentId2);
		Preconditions.checkArgument(tmp.getId() != null,
				"setId or getId has not been implemented correctly by " + entityClass.getCanonicalName());
		Preconditions.checkArgument(tmp.getId().equals(parentId2),
				"setId or getId has not been implemented correctly by " + entityClass.getCanonicalName());
		return tmp;
	}

	@Override
	protected void resetFilters()
	{
		try
		{
			container.removeAllContainerFilters();
			container.addContainerFilter(parentFilter);
		}
		catch (UnsupportedFilterException e)
		{
			ErrorWindow.showErrorWindow(e);
		}
	}

	/**
	 * parent crud will check if the children are dirty, before allowing a row
	 * change
	 */
	@Override
	public boolean isDirty()
	{
		boolean ret = false;
		// call the super to see if the fields are dirty and then also check
		// that records haven't been added or removed
		ret = super.isDirty() || dirty || inNew || areNonFieldGroupFieldsDirty();

		return ret;

	}

	public Object getParentId()
	{
		return parentId;
	}

	@Override
	protected Component getTitle()
	{
		// no title in child cruds
		return null;
	}

	// disabled as the save/cancel enable/disable is buggy
	@Override
	public void fieldGroupIsDirty(boolean b)
	{
		// if (b)
		// {
		// dirty = true;
		// }
		// parentCrud.fieldGroupIsDirty(dirty);
	}

	@Override
	public void discard()
	{
		fieldGroup.discard();
		container.discard();
		for (ChildCrudListener<E> child : childCrudListeners)
		{
			child.discard();
		}
		if (newEntity != null)
		{
			if (restoreDelete)
			{
				activateEditMode(false);
				restoreDelete = false;
			}
			newEntity = null;

			entityTable.select(null);
			if (entityTable.getCurrent() == null)
			{
				showNoSelectionMessage();
			}
			searchField.setReadOnly(false);
		}

	}

	/**
	 * noop event handler
	 *
	 * @return
	 */
	private ChildCrudEventHandler<E> getNullEventHandler()
	{
		return new ChildCrudEventHandler<E>()
		{
			@Override
			public void entitiesAdded(Collection<E> entities)
			{
			}

			@Override
			public void entitiesUpdated(Collection<E> entities)
			{
			}

			@Override
			public void entitiesDeleted(Collection<E> entities)
			{
			}
		};
	}

	@Override
	protected String getNewButtonLabel()
	{
		return getNewButtonActionLabel();
	}

	/**
	 * it gets confusing with multiple new buttons that appear when using nested
	 * cruds, so provide a label like "New Goal" to ease the confusion
	 *
	 * @return
	 */
	public abstract String getNewButtonActionLabel();
}
