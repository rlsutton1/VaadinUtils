package au.com.vaadinutils.crud;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.Field;

public class ValidatingFieldGroup<E> extends FieldGroup
{
	private static final long serialVersionUID = 1L;
	private final Class<E> entityClass;
	private JPAContainer<E> container;

	Logger logger = LogManager.getLogger();

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

	private boolean groupIsDirty;
	private DirtyListener dirtyListener;

	public void setDirtyListener(DirtyListener listener)
	{
		dirtyListener = listener;
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

			ValueChangeListener changeListener = new ValueChangeListener()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void valueChange(ValueChangeEvent event)
				{
					if (groupIsDirty == false)
					{
						groupIsDirty = true;

						if (dirtyListener != null)
						{
							dirtyListener.fieldGroupIsDirty(true);
						}

					}

				}
			};
			field.addValueChangeListener(changeListener);
		}
		knownFields.add(field);

	}

	public void discard()
	{
		groupIsDirty = false;
		if (dirtyListener != null)
		{
			dirtyListener.fieldGroupIsDirty(false);
		}
		super.discard();

	}

	public void setItemDataSource(Item itemDataSource)
	{
		groupIsDirty = false;
		if (dirtyListener != null)
		{
			dirtyListener.fieldGroupIsDirty(false);
		}
		super.setItemDataSource(itemDataSource);
	}

	public JPAContainer<E> getContainer()
	{

		return container;
	}

	// when finished testing delete this method, it's in the super type anyway
	@Override
	public boolean isModified()
	{
		for (Field<?> field : getFields())
		{
			if (field.isModified())
			{
				logger.warn("Dirty: {} {}", field.getCaption(), field.getClass().getSimpleName());
				logger.warn("Dirty value: " + field.getValue());

				return true;
			}
		}
		return false;
	}

	public void bind(Field<?> field, Object propertyId) throws BindException
	{

		boolean isReadonly = false;
		try
		{
			isReadonly = field.isReadOnly();
			field.setReadOnly(false);
			super.bind(field, propertyId);

		}
		finally
		{
			field.setReadOnly(isReadonly);
		}
	}

}
