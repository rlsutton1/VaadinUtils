package au.com.vaadinutils.crud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import au.com.vaadinutils.crud.splitFields.SplitCheckBox;
import au.com.vaadinutils.crud.splitFields.SplitComboBox;
import au.com.vaadinutils.crud.splitFields.SplitDateField;
import au.com.vaadinutils.crud.splitFields.SplitEditorField;
import au.com.vaadinutils.crud.splitFields.SplitLabel;
import au.com.vaadinutils.crud.splitFields.SplitPasswordField;
import au.com.vaadinutils.crud.splitFields.SplitTextArea;
import au.com.vaadinutils.crud.splitFields.SplitTextField;
import au.com.vaadinutils.fields.CKEditorEmailField;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectConverter;
import com.vaadin.data.Container;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FormHelper<E> implements Serializable
{
	private static final long serialVersionUID = 1L;
	static EntityManagerFactory entityManagerFactory;

	ArrayList<AbstractComponent> fieldList = new ArrayList<AbstractComponent>();
	private AbstractLayout form;
	private ValidatingFieldGroup<E> group;

	public FormHelper(AbstractLayout form, ValidatingFieldGroup<E> group)
	{
		this.form = form;
		this.group = group;
	}

	/**
	 * Provides a factory which allows the FormHelper to get the current
	 * EntityManager as and when it needs it.
	 * 
	 * @param factory
	 */
	static public void setEntityManagerFactory(EntityManagerFactory factory)
	{
		entityManagerFactory = factory;
	}

	public <M> TextField bindTextField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			SingularAttribute<E, M> member)
	{
		TextField field = bindTextField(form, group, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;
	}

	public <M> TextField bindTextField(String fieldLabel, SingularAttribute<E, M> member)
	{
		TextField field = bindTextField(form, group, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;
	}

	public TextField bindTextField(String fieldLabel, String fieldName)
	{
		TextField field = bindTextField(form, group, fieldLabel, fieldName);
		this.fieldList.add(field);
		return field;
	}

	public TextField bindTextField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName)
	{
		TextField field = new SplitTextField(fieldLabel);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		field.setNullSettingAllowed(false);
		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;
	}

	
	public <M> PasswordField bindPasswordField(AbstractLayout form, FieldGroup group, String fieldLabel,
			SingularAttribute<E, M> member)
	{
		PasswordField field = bindPasswordField(form, group, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;
	}

	public PasswordField bindPasswordField(AbstractLayout form, FieldGroup group, String fieldLabel, String fieldName)
	{
		PasswordField field = new SplitPasswordField(fieldLabel);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		field.setNullSettingAllowed(false);
		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;
	}

	public <M> TextArea bindTextAreaField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			SingularAttribute<E, M> member, int rows)
	{
		TextArea field = bindTextAreaField(form, group, fieldLabel, member.getName(), rows);
		this.fieldList.add(field);
		return field;
	}

	public TextArea bindTextAreaField(String fieldLabel, String fieldName, int rows)
	{
		TextArea field = bindTextAreaField(form, group, fieldLabel, fieldName, rows);
		this.fieldList.add(field);
		return field;
	}

	public TextArea bindTextAreaField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName, int rows)
	{
		TextArea field = new SplitTextArea(fieldLabel);
		field.setRows(rows);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;
	}

	public DateField bindDateField(String fieldLabel, String fieldName)
	{
		DateField field = bindDateField(form, group, fieldLabel, fieldName);
		this.fieldList.add(field);
		return field;
	}

	public <M> DateField bindDateField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			SingularAttribute<E, M> member, String dateFormat, Resolution resolution)
	{
		DateField field = bindDateField(form, group, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;
	}

	public DateField bindDateField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName, String dateFormat, Resolution resolution)
	{
		DateField field = new SplitDateField(fieldLabel);
		field.setDateFormat(dateFormat);
		field.setResolution(resolution);

		field.setImmediate(true);
		field.setWidth("100%");
		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;
	}

	public DateField bindDateField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName)
	{
		return bindDateField(form, group, fieldLabel, fieldName, "yyyy-MM-dd", Resolution.DAY);
	}

	public Label bindLabel(String fieldLabel)
	{
		Label field = bindLabel(form, group, fieldLabel);
		this.fieldList.add(field);
		return field;
	}

	public Label bindLabel(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel)
	{
		Label field = new SplitLabel(fieldLabel);
		field.setWidth("100%");
		form.addComponent(field);
		return field;
	}

	public Label bindLabel(AbstractLayout form, ValidatingFieldGroup<E> group, Label field)
	{
		field.setWidth("100%");
		form.addComponent(field);
		return field;
	}

	public <M> ComboBox bindEnumField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			SingularAttribute<E, M> member, Class<?> clazz)
	{
		ComboBox field = bindEnumField(form, group, fieldLabel, member.getName(), clazz);
		this.fieldList.add(field);
		return field;
	}

	public ComboBox bindEnumField(String fieldLabel, String fieldName, Class<?> clazz)
	{
		ComboBox field = bindEnumField(form, group, fieldLabel, fieldName, clazz);
		this.fieldList.add(field);
		return field;
	}

	public ComboBox bindEnumField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName, Class<?> clazz)
	{
		ComboBox field = new SplitComboBox(fieldLabel, createContainerFromEnumClass(fieldName, clazz));
		field.setItemCaptionPropertyId(fieldName);
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setWidth("100%");
		field.setImmediate(true);
		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;
	}

	public <M> CheckBox bindBooleanField(String fieldLabel, SingularAttribute<E, M> member)
	{
		CheckBox field = bindBooleanField(form, group, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;
	}

	public CheckBox bindBooleanField(String fieldLabel, String fieldName)
	{
		CheckBox field = bindBooleanField(form, group, fieldLabel, fieldName);
		this.fieldList.add(field);
		return field;
	}

	public CheckBox bindBooleanField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			SingularAttribute<E, Boolean> member)
	{
		CheckBox field = bindBooleanField(form, group, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;

	}

	public CheckBox bindBooleanField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName)
	{
		CheckBox field = new SplitCheckBox(fieldLabel);
		field.setWidth("100%");
		field.setImmediate(true);

		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;

	}

	public <L> ComboBox bindComboBox(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup, String fieldLabel,
			Collection<?> options)
	{
		ComboBox field = new SplitComboBox(fieldLabel, options);
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setWidth("100%");
		field.setImmediate(true);
		form.addComponent(field);
		return field;
	}

	public <M> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, M> member, Class<E> listClazz,
			SingularAttribute<E, M> listMember)
	{
		ComboBox field = bindEntityField(form, group, fieldLabel, member.getName(), listClazz, listMember.getName());
		this.fieldList.add(field);
		return field;
	}

	public ComboBox bindEntityField(String fieldLabel, String fieldName, Class<E> listClazz, String listFieldname)
	{
		ComboBox field = bindEntityField(form, group, fieldLabel, fieldName, listClazz, listFieldname);
		this.fieldList.add(field);
		return field;
	}

	/**
	 * this method is for displaying a combobox which displayes all the values
	 * for a many to one relationship and allows the user to select only one.
	 * 
	 * @param form
	 * @param fieldGroup
	 * @param fieldLabel
	 * @param field
	 * @param listClazz
	 * @param listFieldName
	 * 
	 *            E is the entity F is the set from the entity that contains the
	 *            foriegn entities L is the foriegn entity M is the field to
	 *            display from the foriegn entity
	 * 
	 * @return
	 */
	public <F, L, M> ComboBox bindEntityField(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup,
			String fieldLabel, SingularAttribute<E, F> field, Class<L> listClazz, SingularAttribute<L, M> listFieldName)
	{
		return bindEntityField(form, fieldGroup, fieldLabel, field.getName(), listClazz, listFieldName.getName());

	}

	public <L> ComboBox bindEntityField(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup, String fieldLabel,
			String fieldName, Class<L> listClazz, String listFieldName)
	{
		Preconditions.checkNotNull(entityManagerFactory,
				"You must provide the entity manager factory by calling setEntityManager first.");
		JPAContainer<?> container = JPAContainerFactory.make(listClazz, entityManagerFactory.getEntityManager());

		ComboBox field = new SplitComboBox(fieldLabel);

		field.setItemCaptionMode(ItemCaptionMode.PROPERTY);
//		Preconditions.checkState(container.getContainerPropertyIds().contains(listFieldName), listFieldName
//				+ " is not valid, valid listFieldNames are " + container.getContainerPropertyIds().toString());
		field.setItemCaptionPropertyId(listFieldName);
		field.setContainerDataSource(container);
		SingleSelectConverter<L> converter = new SingleSelectConverter<L>(field);
		field.setConverter(converter);
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setWidth("100%");
		field.setImmediate(true);
		if (fieldGroup != null)
		{

			Preconditions.checkState(fieldGroup.getContainer().getContainerPropertyIds().contains(fieldName), fieldName
					+ " is not valid, valid listFieldNames are "
					+ fieldGroup.getContainer().getContainerPropertyIds().toString());

			fieldGroup.bind(field, fieldName);
		}
		form.addComponent(field);
		return field;
	}
	
	public <M> CKEditorEmailField bindEditorField(AbstractLayout form, ValidatingFieldGroup<E> group, 
			SingularAttribute<E, M> member, boolean readonly)
	{
		CKEditorEmailField field = bindEditorField(form, group,  member.getName(), readonly);
		this.fieldList.add(field);
		return field;
	}

	public <M> CKEditorEmailField bindEditorField(String fieldLabel, SingularAttribute<E, M> member, boolean readonly)
	{
		CKEditorEmailField field = bindEditorField(form, group,  member.getName(), readonly);
		this.fieldList.add(field);
		return field;
	}

	public CKEditorEmailField bindEditorField( String fieldName, boolean readonly)
	{
		CKEditorEmailField field = bindEditorField(form, group,  fieldName,  readonly);
		this.fieldList.add(field);
		return field;
	}

	public CKEditorEmailField bindEditorField(AbstractLayout form, ValidatingFieldGroup<E> group, 
			String fieldName, boolean readonly)
	{
		SplitEditorField field = new SplitEditorField(readonly);
		field.setWidth("100%");
		field.setImmediate(true);
		if (group != null)
			group.bind(field, fieldName);
		form.addComponent(field);
		return field;
	}


	static Container createContainerFromEnumClass(String fieldName, Class<?> clazz)
	{
		LinkedHashMap<Enum<?>, String> enumMap = new LinkedHashMap<Enum<?>, String>();
		for (Object enumConstant : clazz.getEnumConstants())
		{
			enumMap.put((Enum<?>) enumConstant, enumConstant.toString());
		}

		return createContainerFromMap(fieldName, enumMap);
	}

	@SuppressWarnings("unchecked")
	static public Container createContainerFromMap(String fieldName, Map<?, String> hashMap)
	{
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty(fieldName, String.class, "");

		Iterator<?> iter = hashMap.keySet().iterator();
		while (iter.hasNext())
		{
			Object itemId = iter.next();
			container.addItem(itemId);
			container.getItem(itemId).getItemProperty(fieldName).setValue(hashMap.get(itemId));
		}

		return container;
	}

	public ArrayList<AbstractComponent> getFieldList()
	{
		return this.fieldList;
	}

	public static void showConstraintViolation(ConstraintViolationException e)
	{
		// build constraint error
		StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<?> violation : e.getConstraintViolations())
		{
			sb.append("Error: " + violation.getPropertyPath() + " : " + violation.getMessage() + "\n");
		}
		Notification.show(sb.toString(), Type.ERROR_MESSAGE);
	}

	public boolean isEntitymanagerSet()
	{
		return entityManagerFactory != null;
	}

	protected AbstractLayout getForm()
	{
		return form;
	}

	protected ValidatingFieldGroup<E> getFieldGroup()
	{
		return this.group;
	}

}
