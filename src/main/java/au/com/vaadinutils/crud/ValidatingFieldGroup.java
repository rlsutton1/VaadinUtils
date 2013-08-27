package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.Field;

public class ValidatingFieldGroup<E> extends FieldGroup
{
	private static final long serialVersionUID = 1L;
	private final Class<E> entityClass;
	private JPAContainer<E> container;


	public ValidatingFieldGroup(JPAContainer<E> container, Class<E> entityClass)
	{
		this.entityClass = entityClass;
		this.container = container;
	}

	
	public ValidatingFieldGroup(Item item, Class<E> entityClass)
	{
		super(item);
		this.entityClass = entityClass;
		
	}

	/*
	 * Override configureField to add a bean validator to each field.
	 */
	@Override
	protected void configureField(Field<?> field)
	{
		field.removeAllValidators();
		super.configureField(field);
		// Add Bean validators if there are annotations
		// Note that this requires a bean validation implementation to
		// be available.
		BeanValidator validator = new BeanValidator(entityClass, getPropertyId(field).toString());
		
		field.addValidator(validator);
		if (field.getLocale() != null)
		{
			validator.setLocale(field.getLocale());
		}
	}


	public JPAContainer<E> getContainer()
	{

		return container;
	}

}
