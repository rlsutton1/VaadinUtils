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

import org.apache.log4j.Logger;

import au.com.vaadinutils.crud.splitFields.SplitCheckBox;
import au.com.vaadinutils.crud.splitFields.SplitComboBox;
import au.com.vaadinutils.crud.splitFields.SplitDateField;
import au.com.vaadinutils.crud.splitFields.SplitEditorField;
import au.com.vaadinutils.crud.splitFields.SplitLabel;
import au.com.vaadinutils.crud.splitFields.SplitPasswordField;
import au.com.vaadinutils.crud.splitFields.SplitTextArea;
import au.com.vaadinutils.crud.splitFields.SplitTextField;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.fields.CKEditorEmailField;
import au.com.vaadinutils.fields.DataBoundButton;

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
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FormHelper<E> implements Serializable
{
	private static final long serialVersionUID = 1L;

	ArrayList<AbstractComponent> fieldList = new ArrayList<AbstractComponent>();
	private AbstractLayout form;
	private ValidatingFieldGroup<E> group;

	static Logger logger = Logger.getLogger(FormHelper.class);

	public FormHelper(AbstractLayout form, ValidatingFieldGroup<E> group)
	{
		// I'm actually using this without a field group.
		// need to makes some modes so that we formally support non-group usage.
		// Preconditions.checkNotNull(group,
		// "ValidatingFieldGroup can not be null");
		this.form = form;
		this.group = group;
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
		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}

	private void doBinding(FieldGroup group, String fieldName, @SuppressWarnings("rawtypes") Field field)
	{
		if (group != null)
			group.bind(field, fieldName);
		else

		{
			logger.warn("field " + fieldName + " was not bound");
		}
	}

	public <M> PasswordField bindPasswordField(AbstractLayout form, FieldGroup group, String fieldLabel,
			SingularAttribute<E, M> member)
	{
		PasswordField field = bindPasswordField(form, group, fieldLabel, (member != null ? member.getName() : null));
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
		doBinding(group, fieldName, field);
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
		doBinding(group, fieldName, field);
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
		DateField field = bindDateField(form, group, fieldLabel, member.getName(), dateFormat, resolution);
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
		doBinding(group, fieldName, field);
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
		return bindEnumField(new SplitComboBox(fieldLabel), form, group, fieldLabel, fieldName, clazz);
	}

	public ComboBox bindEnumField(ComboBox comboBox, AbstractLayout form, ValidatingFieldGroup<E> group,
			String fieldLabel, String fieldName, Class<?> clazz)
	{
		ComboBox field = comboBox;
		field.setCaption(fieldLabel);
		field.setContainerDataSource(createContainerFromEnumClass(fieldName, clazz));
		field.setItemCaptionPropertyId(fieldName);
		// field.setCaption(fieldLabel);
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setWidth("100%");
		field.setImmediate(true);
		doBinding(group, fieldName, field);

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

		doBinding(group, fieldName, field);

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

	@Deprecated
	public <L> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, L> fieldName, Class<L> listClazz,
			SingularAttribute<L, ?> listFieldName)
	{
		return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListClass(listClazz)
				.setListFieldName(listFieldName).build();

	}

	@Deprecated
	public <L> ComboBox bindEntityField(String fieldLabel, String fieldName, Class<L> listClazz, String listFieldName)
	{
		return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListClass(listClazz)
				.setListFieldName(listFieldName).build();

	}

	/**
	 * Deprecated - use EntityFieldBuilder instead
	 * 
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

	/**
	 * Deprecated - use EntityFieldBuilder instead
	 * 
	 * @param form
	 * @param fieldGroup
	 * @param fieldLabel
	 * @param field
	 * @param listClazz
	 * @param listFieldName
	 * @return
	 */
	@Deprecated
	public <L> ComboBox bindEntityField(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup, String fieldLabel,
			SingularAttribute<E, L> field, Class<L> listClazz, SingularAttribute<L, ?> listFieldName)
	{
		return new EntityFieldBuilder<L>().setForm(form).setLabel(fieldLabel).setField(field)
				.setListFieldName(listFieldName).build();

	}

	/**
	 * Deprecated - use EntityFieldBuilder instead
	 * 
	 * @param form
	 * @param fieldGroup
	 * @param fieldLabel
	 * @param fieldName
	 * @param listClazz
	 * @param listFieldName
	 * @return
	 */
	@Deprecated
	public <L> ComboBox bindEntityField(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup, String fieldLabel,
			String fieldName, Class<L> listClazz, String listFieldName)
	{
		return new EntityFieldBuilder<L>().setForm(form).setLabel(fieldLabel).setField(fieldName)
				.setListClass(listClazz).setListFieldName(listFieldName).build();

	}

	/**
	 * Deprecated - use EntityFieldBuilder instead
	 * 
	 * @param field
	 * @param form
	 * @param fieldGroup
	 * @param fieldLabel
	 * @param fieldName
	 * @param listClazz
	 * @param listFieldName
	 * @return
	 */
	@Deprecated
	public <L> ComboBox bindEntityField(ComboBox field, AbstractLayout form, ValidatingFieldGroup<E> fieldGroup,
			String fieldLabel, String fieldName, Class<L> listClazz, String listFieldName)
	{
		return new EntityFieldBuilder<L>().setComponent(field).setForm(form).setLabel(fieldLabel).setField(fieldName)
				.setListClass(listClazz).setListFieldName(listFieldName).build();

	}

	/**
	 *  use this syntax to instance the builder:<br>
	 *  	formHelper.new EntityFieldBuilder<{name of list class}>();
	 *  <br><br>
	 *  	for example<br><br>
	 *  	
	 *  	FormHelper<Tblroutestep> helper = new FormHelper<Tblroutestep>(...);<br><br>
	 *   	ComboBox field = helper.new EntityFieldBuilder<Tblroutingscript>()<br>
	 *   	.setLabel("Action")<br>
	 *   	.setField(Tblroutestep_.script)<br>
	 *   	.setListFieldName(Tblroutingscript_.name)<br>
	 *   	.build();<br>
	 * @author rsutton
	 * 
	 * @param <L> the type of the list class
	 */
	public class EntityFieldBuilder<L>
	{

		private ComboBox component = null;
		private String label = null;
		private JPAContainer<L> container = null;
		private Class<L> listClazz;
		private String listField;
		// private ValidatingFieldGroup<E> fieldGroup;
		private String field;
		private AbstractLayout builderForm;

		public ComboBox build()
		{
			Preconditions.checkNotNull(label, "Label may not be null");
			Preconditions.checkNotNull(listField, "ListField may not be null");
			Preconditions.checkNotNull(field, "Field may not be null");
			if (builderForm == null)
			{
				builderForm = form;
	}
			Preconditions.checkNotNull(builderForm, "Form may not be null");
	
			if (component == null)
	{
				component = new SplitComboBox(label);
			}
			component.setCaption(label);
			component.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		
			if (container == null)
			{
				Preconditions.checkNotNull(listClazz, "listClazz may not be null");
				container = JPAContainerFactory.make(listClazz, EntityManagerProvider.getEntityManager());

			}

			Preconditions.checkState(container.getContainerPropertyIds().contains(listField), listField
				+ " is not valid, valid listFieldNames are " + container.getContainerPropertyIds().toString());

			if (container.getSortableContainerPropertyIds().contains(listField))
				container.sort(new String[] { listField }, new boolean[] { true });

			component.setItemCaptionPropertyId(listField);
			component.setContainerDataSource(container);
			SingleSelectConverter<L> converter = new SingleSelectConverter<L>(component);
			component.setConverter(converter);
			component.setNewItemsAllowed(false);
			component.setNullSelectionAllowed(false);
			component.setTextInputAllowed(false);
			component.setWidth("100%");
			component.setImmediate(true);
			if (group != null)
		{
				Preconditions.checkState(group.getContainer().getContainerPropertyIds().contains(field), field
					+ " is not valid, valid listFieldNames are "
						+ group.getContainer().getContainerPropertyIds().toString());

				doBinding(group, field, component);
			}
			builderForm.addComponent(component);
			return component;
		}

		public EntityFieldBuilder<L> setContainer(JPAContainer<L> container)
		{
			this.container = container;
			return this;
		}

		public EntityFieldBuilder<L> setForm(AbstractLayout form)
		{
			this.builderForm = form;
			return this;
		}

		public EntityFieldBuilder<L> setLabel(String label)
		{
			this.label = label;
			return this;
		}

		public EntityFieldBuilder<L> setComponent(ComboBox component)
		{
			this.component = component;
			return this;
		}

		public EntityFieldBuilder<L> setField(SingularAttribute<E, L> field)
		{
			this.field = field.getName();
			listClazz = field.getJavaType();
			return this;
		}

		public EntityFieldBuilder<L> setListFieldName(SingularAttribute<L, ?> listField)
		{
			this.listField = listField.getName();
			return this;
		}

		public EntityFieldBuilder<L> setField(String field)
		{
			this.field = field;
			return this;
		}

		public EntityFieldBuilder<L> setListFieldName(String listField)
		{
			this.listField = listField;
			return this;
		}

		public EntityFieldBuilder<L> setListClass(Class<L> listClazz)
		{
			Preconditions.checkState(this.listClazz == null,
					"If you set the field as a singularAttribute, the listClass is set automatically.");
			this.listClazz = listClazz;
			return this;
		}

	}

	public <M> CKEditorEmailField bindEditorField(AbstractLayout form, ValidatingFieldGroup<E> group,
			SingularAttribute<E, M> member, boolean readonly)
	{
		CKEditorEmailField field = bindEditorField(form, group, member.getName(), readonly);
		this.fieldList.add(field);
		return field;
	}

	public <M> CKEditorEmailField bindEditorField(String fieldLabel, SingularAttribute<E, M> member, boolean readonly)
	{
		CKEditorEmailField field = bindEditorField(form, group, member.getName(), readonly);
		this.fieldList.add(field);
		return field;
	}

	public CKEditorEmailField bindEditorField(String fieldName, boolean readonly)
	{
		CKEditorEmailField field = bindEditorField(form, group, fieldName, readonly);
		this.fieldList.add(field);
		return field;
	}

	public CKEditorEmailField bindEditorField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldName,
			boolean readonly)
	{
		SplitEditorField field = new SplitEditorField(readonly);
		field.setWidth("100%");
		field.setImmediate(true);
		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}
	
//	public <L> EntityAutoCompleteField bindAutoCompleteField(AutoCompleteParent<E> parent, 
//			String fieldLabel, ListAttribute<E, L> entityField, Class<L> listClazz)
//	{
//		// hack
//		ContactDao dao = new DaoFactory().getContactDao();
//		container = dao.createVaadinContainer();
//		
//		//EntityAutoCompleteField<CrudEntity, JpaBaseDao<E,Long>> field = new EntityAutoCompleteField<CrudEntity, JpaBaseDao<E,Long>>(container, dao, fieldLabel, parent);
//		EntityAutoCompleteField field = new EntityAutoCompleteField<CrudEntity, JpaBaseDao(container, dao, fieldLabel, parent);
//		return field;
//				
//	}

	public static Container createContainerFromEnumClass(String fieldName, Class<?> clazz)
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

	protected AbstractLayout getForm()
	{
		return form;
	}

	protected ValidatingFieldGroup<E> getFieldGroup()
	{
		return this.group;
	}

	public <M> DataBoundButton<M> bindButtonField(String fieldLabel, SingularAttribute<E, M> enterScript, Class<M> type)
	{
		return bindButtonField(fieldLabel, enterScript.getName(), type);

	}

	public <M> DataBoundButton<M> bindButtonField(String fieldLabel, String fieldName, Class<M> type)
	{
		DataBoundButton<M> field = bindButtonField(form, group, fieldLabel, fieldName, type);
		this.fieldList.add(field);
		return field;
	}

	public <M> DataBoundButton<M> bindButtonField(AbstractLayout form, ValidatingFieldGroup<E> group,
			String fieldLabel, String fieldName, Class<M> type)
	{
		DataBoundButton<M> field = new DataBoundButton<M>(fieldLabel, type);

		field.setImmediate(true);

		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}

	
}
