package au.com.vaadinutils.ui;

import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.teemu.wizards.WizardStep;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.FormHelper;
import au.com.vaadinutils.crud.ValidatingFieldGroup;
import au.com.vaadinutils.dao.JpaBaseDao;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public abstract class SingleEntityWizardStep<E extends CrudEntity> implements WizardStep
{
	private static Logger logger = LogManager.getLogger(SingleEntityWizardStep.class);

	private au.com.vaadinutils.crud.ValidatingFieldGroup<E> fieldGroup;
	private JPAContainer<E> container;
	private E entity;

	private boolean isNew = false;

	private Class<E> entityClass;

	private Component editor = null;

	public SingleEntityWizardStep(JpaBaseDao<E, Long> entityDao, Class<E> entityClass)
	{
		this.entityClass = entityClass;
		this.container = entityDao.createVaadinContainer();
		fieldGroup = new ValidatingFieldGroup<E>(entityClass);

	}

	@Override
	public String getCaption()
	{
		return "Group Details";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getContent()
	{
		if (editor == null)
		{
			this.entity = findEntity();
			EntityItem<E> entityItem;
			if (entity == null)
			{
				try
				{
					isNew = true;
					entity = entityClass.newInstance();
					initEntity(entity);
					entityItem = container.createEntityItem(entity);
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
			else
			{
				isNew = false;
				Long itemId = entity.getId();
				entityItem = container.getItem(itemId);
				// As we did a lookup the entity we retrieved during the lookup
				// may not be
				// the one we retrieve from the container.
				entity = entityItem.getEntity();
			}
			Preconditions.checkArgument(entityItem != null);
			Preconditions.checkArgument(entity == entityItem.getEntity());
			
			fieldGroup.setItemDataSource(entityItem);
			
			editor = getContent(fieldGroup);
		}

		Preconditions.checkArgument(((EntityItem<E>) fieldGroup.getItemDataSource()).getEntity() == entity);
		return editor;
	}

	/**
	 * Do any custom initialisation of a new entity.
	 * 
	 * @param entity
	 */
	abstract protected void initEntity(E entity);

	/**
	 * 
	 * Search for an existing entity to edit or return null if one doesn't
	 * exist.
	 * 
	 * @return
	 */
	abstract protected E findEntity();

	/**
	 * Build the layout used for editing the entity Any fields must be bound to
	 * the field Group.
	 * 
	 * @param fieldGroup
	 * @return
	 */
	abstract protected Component getContent(ValidatingFieldGroup<E> fieldGroup);

	@Override
	public boolean onAdvance()
	{
		return validate();
	}

	protected boolean validate()
	{
		boolean valid = false;
		try
		{
			if (!fieldGroup.isValid())
			{
				Notification.show("Validation Errors", "Please fix any field errors and try again.",
						Type.WARNING_MESSAGE);
			}
			else
			{
				fieldGroup.commit();
				if (isNew)
				{
					Object id = container.addEntity(entity);
					EntityItem<E> entityItem = container.getItem(id);
					entity = entityItem.getEntity();
					fieldGroup.setItemDataSource(entityItem);
					isNew = false;
				}
				container.commit();
				// entity = container.getItem(entity.getId()).getEntity();

				valid = true;
				Notification.show("The details have been saved.", Type.TRAY_NOTIFICATION);
			}
		}
		catch (ConstraintViolationException e)
		{
			FormHelper.showConstraintViolation(e);
		}
		catch (CommitException e)
		{
			logger.error(e, e);
			if (e.getCause() instanceof ConstraintViolationException)
				FormHelper.showConstraintViolation(((ConstraintViolationException) e.getCause()));
			else
				Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}
		return valid;
	}

	@Override
	public boolean onBack()
	{
		return true;
	}

	public E getEntity()
	{
		return this.entity;
	}

	protected ValidatingFieldGroup<E> getFieldGroup()
	{
		return fieldGroup;
	}

}
