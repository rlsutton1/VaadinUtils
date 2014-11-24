package au.com.vaadinutils.crud;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent;
import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent.EntitiesRemovedEvent;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainer.ProviderChangedEvent;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/**
 * child crud does not support nesting.
 * 
 * @author rsutton
 * 
 * @param <P>
 * @param <E>
 */
public abstract class ChildCrudView<P extends CrudEntity, E extends ChildCrudEntity> extends BaseCrudView<E> implements
		ChildCrudListener<P>
{

	private static final long serialVersionUID = -7756584349283089830L;
	transient Logger logger = LogManager.getLogger(ChildCrudView.class);
	private String parentKey;
	protected String childKey;
	private Object parentId;
	protected P currentParent;
	protected Filter parentFilter;
	protected boolean dirty = false;
	final private Class<P> parentType;
	public BaseCrudView<P> parentCrud;
	private ChildCrudEventHandler<E> eventHandler = getNullEventHandler();
	private Class<E> childType;

	/**
	 * 
	 * @param parentKey
	 *            - this will usually be the primary key of the parent table
	 * @param childKey
	 *            - this will be the foreign key in the child table
	 */
	public ChildCrudView(BaseCrudView<P> parent, Class<P> parentType, Class<E> childType,
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

	public ChildCrudView(BaseCrudView<P> parent, Class<P> parentType, Class<E> childType,
			SingularAttribute<? extends CrudEntity, ? extends Object> parentKey, String childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey;
		this.parentType = parentType;
		this.parentCrud = parent;
		// setMargin(true);

	}

	@Override
	protected void init(Class<E> entityClass, JPAContainer<E> container, HeadingPropertySet<E> headings)
	{

		super.init(entityClass, container, headings);
		// ensure auto commit is off, so that child updates don't go to the db
		// until the parent saves
		container.setAutoCommit(false);
		// container.setBuffered(true);

		// child cruds dont have save/cancel buttons
		rightLayout.removeComponent(buttonLayout);

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
		if (getCurrent() != null)
		{
			selectedIdBeforeSave = getCurrent().getId();
		}
		String currentGuid = getCurrent().getGuid();
		saveEditsToTemp();
		for (Object id : container.getItemIds())
		{
			EntityItem<E> item = container.getItem(id);
			EntityItemProperty reference = item.getItemProperty(childKey);
			if (reference == null)
			{
				logger.error("Child key " + childKey + " doesn't exist in the container " + container.getEntityClass());
			}
			if (reference == null || reference.getValue() == null)
			{
				try
				{

					item.getItemProperty(childKey).setValue(translateParentId(newParentId.getId()));

				}
				catch (Exception e)
				{
					logger.error(e, e);
				}

			}

			extendedChildCommitProcessing(newParentId, item);

		}
		// container.commit();
		logger.warn("Committing for " + this.getClass());
		commitContainerWithHooks();

		// on a new parent, the parent id changes and the container becomes
		// empty. so reset the parent filter and refresh the container
		createParentFilter(parentCrud.getContainer().getItem(newParentId.getId()));
		resetFilters();

		// container.discard();
		container.refresh();
		associateChildren(newParentId);
		dirty = false;
		entityTable.select(null);
		if (selectedIdBeforeSave != null)
		{
			entityTable.select(selectedIdBeforeSave);
		}
		else
		{
			if (getGuidAttribute() != null)
			{
				E entity = JpaBaseDao.getGenericDao(childType).findOneByAttribute(getGuidAttribute(), currentGuid);
				entityTable.select(entity.getId());
			}
		}
		triggerFilter();

	}

	/**
	 * return the singluarAttribute for the guid field of the child entity
	 * 
	 * @return
	 */
	abstract public SingularAttribute<E, String> getGuidAttribute();

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

			/**
			 * 
			 */
			private static final long serialVersionUID = 9132090066374531277L;

			@Override
			public void containerItemSetChange(ItemSetChangeEvent event)
			{
				if (event instanceof ProviderChangedEvent)
				{
					@SuppressWarnings("rawtypes")
					ProviderChangedEvent pce = (ProviderChangedEvent) event;
					@SuppressWarnings("unchecked")
					EntityProviderChangeEvent<E> changeEvent = pce.getChangeEvent();
					if (changeEvent instanceof EntitiesRemovedEvent)
					{
						Collection<E> affectedEntitys = changeEvent.getAffectedEntities();
						eventHandler.entitiesDeleted(affectedEntitys);
					}
				}
			}

		};

		try
		{
			// add the listener
			container.addItemSetChangeListener(tmp);
			// call commit
			container.commit();
		}
		finally
		{
			// detach the listener
			container.removeItemSetChangeListener(tmp);
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
			logger.warn(e, e);
		}

		entityTable.removeItem(entityId);
		newEntity = null;

		entityTable.select(null);
		entityTable.select(previousItemId);

		postChildDelete(entityId);

		dirty = true;

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

	public void validateFieldz()
	{
		try
		{
			if (!fieldGroup.isValid())
			{

				throw new InvalidValueException("Fields are invalid");

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
		applyButton.setEnabled(!activate);

		// for child new is always enabled unless explicitly disallowed
		boolean showNew = true;
		if (isDisallowNew())
			showNew = false;
		newButton.setEnabled(showNew);
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
				saveEditsToTemp();
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
	protected void newClicked()
	{

		try
		{
			inNew = true;
			saveEditsToTemp();
			resetFilters();
			triggerFilter();

			createNewEntity();

			// if we call the overridden version we loop indefinitely

			ChildCrudView.super.rowChanged(newEntity);

			// Can't delete when you are adding a new record.
			// Use cancel instead.
			if (applyButton.isVisible())
			{
				restoreDelete = true;
				activateEditMode(true);
				actionLayout.setVisible(true);
			}

			selectFirstFieldAndShowTab();

			postNew(newEntity);

			rightLayout.setVisible(true);
		}
		catch (ConstraintViolationException e)
		{
			FormHelper.showConstraintViolation(e);
		}
		catch (InstantiationException e)
		{
			logger.error(e, e);
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			logger.error(e, e);
			throw new RuntimeException(e);
		}
		catch (Exception e)
		{
			logger.error(e, e);
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

	public void saveEditsToTemp()
	{
		if (saving == false)
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
					}
					else
					{
						EntityItem<E> current = entityTable.getCurrent();
						if (current != null)
						{
							interceptSaveValues(current);
							// container.commit();
						}
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
				logger.error(e, e);
				Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			}
			catch (ConstraintViolationException e)
			{
				logger.error(e, e);
				FormHelper.showConstraintViolation(e);
			}
			catch (InvalidValueException e)
			{
				logger.error(e, e);
				Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			}
			catch (CommitException e)
			{
				if (e.getCause() instanceof InvalidValueException)
				{
					Notification.show("Please fix the form errors and then try again.", Type.ERROR_MESSAGE);
				}
				else
				{
					logger.error(e, e);
					Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
				}
			}
			catch (Exception e)
			{
				logger.error(e, e);
				Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
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
				}
				splitPanel.showFirstComponet();
				saving = false;
			}
		// }
		// }

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

	/**
	 * this method is called when the parent crud changes row, so we set filters
	 * so that this child only displays the associated rows
	 */
	@Override
	public void selectedParentRowChanged(EntityItem<P> item)
	{
		try
		{
			saveEditsToTemp();
			createParentFilter(item);
			currentParent = null;
			if (item != null)
			{
				currentParent = item.getEntity();
			}
			fieldGroup.discard();
			container.discard();
			dirty = false;
			resetFilters();
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
					logger.warn(e, e);
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
			handleConstraintViolationException(e);
			logger.error(e, e);
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
	 */
	protected Object translateParentId(Object parentId2) throws InstantiationException, IllegalAccessException
	{

		Preconditions.checkNotNull(parentId2,
				"attempt to translate null parent id in " + entityClass.getCanonicalName());
		P tmp = parentType.newInstance();
		Preconditions.checkNotNull(tmp, "failed to create instance of " + entityClass.getCanonicalName());
		tmp.setId((Long) parentId2);
		Preconditions.checkArgument(tmp.getId() != null, "setId or getId has not been implemented correctly by "
				+ entityClass.getCanonicalName());
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
			if (parentFilter != null)
			{
				container.addContainerFilter(parentFilter);
			}
		}
		catch (Exception e)
		{
			handleConstraintViolationException(e);

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
		ret = super.isDirty() || dirty || inNew;

		return ret;

	}

	public Object getParentId()
	{
		return parentId;
	}

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
			public void entitiesDeleted(Collection<E> entities)
			{
			}
		};
	}

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
