package au.com.vaadinutils.crud;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
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

	public ValidatingFieldGroup(Class<E> entityClass)
	{
		this.entityClass = entityClass;

	}

	private Set<Field<?>> knownFields = new HashSet<Field<?>>();

	/*
	 * Override configureField to add a bean validator to each field.
	 */
	@Override
	protected void configureField(Field<?> field)
	{

		// Vaadin applies the readonly status from the underlying entity
		// which doesn't allow us to make a single field readonly
		// hence we track it ourselves.
		boolean readOnly = field.isReadOnly();
		super.configureField(field);

		// If the field was originally readonly then force it back to readonly.
		if (readOnly)
			field.setReadOnly(true);

		if (!knownFields.contains(field))
		{
			// only ever add the validator once for a field
			
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
		knownFields.add(field);
	}

	public JPAContainer<E> getContainer()
	{

		return container;
	}

}
