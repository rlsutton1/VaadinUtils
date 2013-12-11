package au.com.vaadinutils.crud;

import java.util.Collection;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import au.com.vaadinutils.dao.EntityManagerProvider;

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
import com.vaadin.ui.UI;

/**
 * child crud does not support nesting.
 * 
 * @author rsutton
 * 
 * @param <P>
 * @param <E>
 */
public abstract class ChildCrudView<P extends CrudEntity, E extends CrudEntity> extends BaseCrudView<E> implements
		ChildCrudListener<P>
{

	private static final long serialVersionUID = -7756584349283089830L;
	Logger logger = Logger.getLogger(ChildCrudView.class);
	private String parentKey;
	protected String childKey;
	private Object parentId;
	private Filter parentFilter;
	protected boolean dirty = false;
	final private Class<P> parentType;
	protected BaseCrudView<P> parentCrud;
	private ChildCrudEventHandler<E> eventHandler = getNullEventHandler();

	/**
	 * 
	 * @param parentKey
	 *            - this will usually be the primary key of the parent table
	 * @param childKey
	 *            - this will be the foreign key in the child table
	 */
	@SuppressWarnings("unchecked")
	public ChildCrudView(BaseCrudView<P> parent, Class<P> parentType, Class<E> childType,
			SingularAttribute<? extends CrudEntity, ? extends Object> parentKey,
			SingularAttribute<? extends CrudEntity, ? extends Object> childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey.getName();
		this.parentType = (Class<P>) parent.getClass();
		this.parentCrud = parent;

		// setMargin(true);

	}



	public ChildCrudView(BaseCrudView<P> parent, Class<P> parentType, Class<E> childType,
			SingularAttribute<? extends CrudEntity, ? extends Object> parentKey, String childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey;
		this.parentType = parentType;
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

	}

	/**
	 * this method is invoked when the parent saves, signaling the children that
	 * they too should save.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void committed(P newParentId) throws Exception
	{
		saveEditsToTemp();
		for (Object id : container.getItemIds())
		{
			EntityItem<E> item = container.getItem(id);
			EntityItemProperty reference = item.getItemProperty(childKey);
			if (reference.getValue() == null)
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
		commitContainerWithHooks();

		// on a new parent, the parent id changes and the container becomes
		// empty. so reset the parent filter and refresh the container
		createParentFilter(parentCrud.getContainer().getItem(newParentId.getId()));
		resetFilters();

		container.refresh();
		associateChildren(newParentId);
		dirty = false;

	}

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

		if (previousItemId != null)
		{
			entityTable.select(previousItemId);
		}

		dirty = true;

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

	@Override
	protected void enableActions(boolean enabled)
	{
		applyButton.setEnabled(enabled);
		actionCombo.setEnabled(enabled);

		// for child new is always enabled
		newButton.setEnabled(true);
	}

	public void allowRowChange(final RowChangeCallback callback)
	{
		try
		{
			if (isDirty() && !fieldGroup.isValid())
			{

				throw new InvalidValueException("Fields are invalid");

			}
		}
		catch (InvalidValueException e)
		{
			ConfirmDialog.show(UI.getCurrent(), "Field Errors", e.getMessage()
					+ ". Continuing will result in those changes being discarded. ", "Continue", "Cancel",
					new ConfirmDialog.Listener()
					{
						private static final long serialVersionUID = 1L;

						public void onClose(ConfirmDialog dialog)
						{
							if (dialog.isConfirmed())
							{
								/*
								 * When an entity is selected from the list, we
								 * want to show that in our editor on the right.
								 * This is nicely done by the FieldGroup that
								 * binds all the fields to the corresponding
								 * Properties in our entity at once.
								 */
								fieldGroup.discard();
								if (restoreDelete)
								{
									enableActions(true);
									restoreDelete = false;
								}

								newEntity = null;

								callback.allowRowChange();

							}
							else
							{
								// User did not confirm so don't allow
								// the change.

							}
						}
					});
			return;
		}
		callback.allowRowChange();

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
				// fieldGroup.discard();
				// container.discard();
				// dirty = false;
				super.rowChanged(item);
				enableActions(true);
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

	protected void newClicked()
	{

		/*
		 * Rows in the Container data model are called Item. Here we add a new
		 * row in the beginning of the list.
		 */
		allowRowChange(new RowChangeCallback()
		{

			@Override
			public void allowRowChange()
			{
				try
				{
					inNew = true;
					saveEditsToTemp();
					resetFilters();

					newEntity = container.createEntityItem(entityClass.newInstance());

					// if we call the overriden version we loop indefinately
					ChildCrudView.super.rowChanged(newEntity);
					// Can't delete when you are adding a new record.
					// Use cancel instead.
					if (applyButton.isVisible())
					{
						restoreDelete = true;
						enableActions(false);
						actionLayout.setVisible(true);
					}

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
				finally
				{
					inNew = false;
				}
			}
		});

	}

	/**
	 * for child crud, dont have save and cancel buttons
	 */
	protected void addSaveAndCancelButtons()
	{

	}

	/**
	 * for child crud, save is implied when the row changes
	 */
	@Override
	public void save()
	{

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

			finally
			{
				if (newEntity != null)
				{
					newEntity = null;
					if (restoreDelete)
					{
						enableActions(true);
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

	/**
	 * this method is called when the parent crud changes row, so we set filters
	 * so that this child only displays the associated rows
	 */
	@Override
	public void selectedRowChanged(EntityItem<P> item)
	{
		try
		{
			saveEditsToTemp();
			createParentFilter(item);
			fieldGroup.discard();
			container.discard();
			dirty = false;
			resetFilters();
			entityTable.select(entityTable.firstItemId());
			searchField.setValue("");
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}

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
		P tmp = parentType.newInstance();
		tmp.setId((Long) parentId2);
		return tmp;
	}

	@Override
	protected void resetFilters()
	{

		container.removeAllContainerFilters();
		container.addContainerFilter(parentFilter);
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
		ret = super.isDirty() || dirty;

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

	}
	
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
}
