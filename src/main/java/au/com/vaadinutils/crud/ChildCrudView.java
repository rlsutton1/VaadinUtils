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
	private String childKey;
	private Object parentId;
	private Filter parentFilter;
	private boolean dirty = false;

	/**
	 * 
	 * @param parentKey
	 *            - this will usually be the primary key of the parent table
	 * @param childKey
	 *            - this will be the foreign key in the child table
	 */
	public ChildCrudView(Class<P> parentType, Class<E> childType, SingularAttribute<P, ? extends Object> parentKey,
			SingularAttribute<E, ? extends Object> childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey.getName();
	//	setMargin(true);

	}

	public ChildCrudView(Class<P> parentType, Class<E> childType, SingularAttribute<P, ? extends Object> parentKey,
			String childKey)
	{
		super(CrudDisplayMode.VERTICAL);
		this.parentKey = parentKey.getName();
		this.childKey = childKey;
	//	setMargin(true);

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
		for (Object id : container.getItemIds())
		{
			EntityItem<E> item = container.getItem(id);
			EntityItemProperty reference = item.getItemProperty(childKey);
			if (reference.getValue() == null)
			{
				item.getItemProperty(childKey).setValue(newParentId.getId());
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
		Object contactId = entityTable.getValue();
		Object previousItemId = entityTable.prevItemId(contactId);
		entityTable.removeItem(contactId);
		newEntity = null;

		entityTable.select(null);
		entityTable.select(previousItemId);

		dirty = true;

	}

	/**
	 * slightly modified save behaviour so that the records are not committed to
	 * the database.
	 */
	@Override
	protected void save()
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
			Notification.show("Changes Saved", "Any changes you have made have been saved.", Type.TRAY_NOTIFICATION);

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

		parentFilter = new Compare.Equal(childKey, -1);
		if (item != null)

		{
			EntityItemProperty key = item.getItemProperty(parentKey);
			Preconditions.checkNotNull(key, "parentKey " + parentKey + " doesn't exist in properties");
			parentId = key.getValue();
			if (parentId != null)
			{
				parentFilter = new Compare.Equal(childKey, parentId);
			}

		}
		fieldGroup.discard();
		container.discard();
		dirty = false;
		resetFilters();
		entityTable.select(entityTable.firstItemId());
		searchField.setValue("");

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
