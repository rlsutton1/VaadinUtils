package au.com.vaadinutils.crud;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.filter.Compare;
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

	/**
	 * 
	 * @param parentKey
	 *            - this will usually be the primary key of the parent table
	 * @param childKey
	 *            - this will be the foreign key in the child table
	 */
	public ChildCrudView(Class<P> parentType, Class<E> childType,
			SingularAttribute<? extends CrudEntity, ? extends Object> parentKey,
			SingularAttribute<? extends CrudEntity, ? extends Object> childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey.getName();
		this.parentType = parentType;

		// setMargin(true);

	}

	public ChildCrudView(Class<P> parentType, Class<E> childType,
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

	}

	/**
	 * this method is invoked when the parent saves, signaling the children that
	 * they too should save.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void committed(P newParentId)
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
		}
		container.commit();
		dirty = false;

	}

	/**
	 * if a row is deleted in the childCrud, then it is dirty for the purposes
	 * of the parent crud
	 */
	@Override
	protected void delete()
	{
		Object entityId = entityTable.getValue();
		Object previousItemId = entityTable.prevItemId(entityId);
		entityTable.removeItem(entityId);
		newEntity = null;

		entityTable.select(null);
		entityTable.select(previousItemId);

		dirty = true;

	}

	protected void newClicked()
	{
		/*
		 * Rows in the Container data model are called Item. Here we add a new
		 * row in the beginning of the list.
		 */

		try
		{
			saveEditsToTemp();
			resetFilters();

			newEntity = container.createEntityItem(entityClass.newInstance());
			rowChanged(newEntity);
			// Can't delete when you are adding a new record.
			// Use cancel instead.
			if (applyButton.isVisible())
			{
				restoreDelete = true;
				showDelete(false);
				deleteLayout.setVisible(true);
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
	protected void save()
	{

	}

	protected void saveEditsToTemp()
	{
		try
		{
			// if a row is saved in the childCrud, then it is dirty for the
			// purposes of the parent crud
			dirty = true;
			commit();

			if (newEntity != null)
			{
				interceptSaveValues(newEntity.getEntity());

				Object id = container.addEntity(newEntity.getEntity());
				EntityItem<E> item = container.getItem(id);
				// container.commit();

				fieldGroup.setItemDataSource(item);
				entityTable.select(item.getItemId());
				// If we leave the save button active, clicking it again
				// duplicates the record
				// rightLayout.setVisible(false);
			}
			else
			{
				E current = entityTable.getCurrent();
				if (current != null)
				{
					interceptSaveValues(current);
					// container.commit();
				}
			}

			if (newEntity != null)
			{
				newEntity = null;
				if (restoreDelete)
				{
					showDelete(true);
					restoreDelete = false;
				}
			}
			splitPanel.showFirstComponet();
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
				if (entityTable.getCurrent() != null)
				{
					container.removeItem(entityTable.getCurrent());
				}
			}
		}

	}

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
			parentFilter = new Compare.Equal(childKey, translateParentId(-1l));
			if (item != null)

			{
				EntityItemProperty key = item.getItemProperty(parentKey);
				Preconditions.checkNotNull(key, "parentKey " + parentKey + " doesn't exist in properties");
				parentId = (Long) key.getValue();
				if (parentId != null)
				{

					parentFilter = new Compare.Equal(childKey, translateParentId(parentId));

				}

			}
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
	protected P translateParentId(Object parentId2) throws InstantiationException, IllegalAccessException
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
}
