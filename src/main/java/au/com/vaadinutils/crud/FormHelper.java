package au.com.vaadinutils.crud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import au.com.vaadinutils.converter.ContainerAdaptor;
import au.com.vaadinutils.converter.ContainerAdaptorFactory;
import au.com.vaadinutils.converter.MultiSelectConverter;
import au.com.vaadinutils.crud.splitFields.SplitCheckBox;
import au.com.vaadinutils.crud.splitFields.SplitColorPicker;
import au.com.vaadinutils.crud.splitFields.SplitComboBox;
import au.com.vaadinutils.crud.splitFields.SplitDateField;
import au.com.vaadinutils.crud.splitFields.SplitEditorField;
import au.com.vaadinutils.crud.splitFields.SplitLabel;
import au.com.vaadinutils.crud.splitFields.SplitListSelect;
import au.com.vaadinutils.crud.splitFields.SplitPasswordField;
import au.com.vaadinutils.crud.splitFields.SplitTextArea;
import au.com.vaadinutils.crud.splitFields.SplitTextField;
import au.com.vaadinutils.crud.splitFields.SplitTwinColSelect;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.domain.iColor;
import au.com.vaadinutils.domain.iColorFactory;
import au.com.vaadinutils.fields.CKEditorEmailField;
import au.com.vaadinutils.fields.CKEditorEmailField.ConfigModifier;
import au.com.vaadinutils.fields.ColorPickerField;
import au.com.vaadinutils.fields.DataBoundButton;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectConverter;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FormHelper<E extends CrudEntity> implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String STANDARD_COMBO_WIDTH = "220";

	ArrayList<AbstractComponent> fieldList = new ArrayList<AbstractComponent>();
	private AbstractLayout form;
	private ValidatingFieldGroup<E> group;
	private Set<ValueChangeListener> valueChangeListeners = new LinkedHashSet<>();

	static transient Logger logger = LogManager.getLogger(FormHelper.class);

	public FormHelper(AbstractLayout form, ValidatingFieldGroup<E> group)
	{
		// I'm actually using this without a field group.
		// need to makes some modifications so that we formally support
		// non-group usage.
		// Preconditions.checkNotNull(group,
		// "ValidatingFieldGroup can not be null");
		this.form = form;
		this.group = group;
	}

	/**
	 * The added value change listener will get added to every component that's
	 * created with the FormHelper
	 *
	 * @param listener
	 *            the value change listener
	 */
	public void addValueChangeListener(ValueChangeListener listener)
	{
		valueChangeListeners.add(listener);
	}

	@SuppressWarnings("rawtypes")
	private void addValueChangeListeners(AbstractField c)
	{
		for (ValueChangeListener listener : valueChangeListeners)
		{
			c.addValueChangeListener(listener);
		}
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

	public <M> TextField bindTextFieldWithButton(String fieldLabel, SingularAttribute<E, M> member, Button button)
	{

		TextField field = bindTextFieldWithButton(form, group, fieldLabel, member.getName(), button);

		this.fieldList.add(field);

		return field;
	}

	public TextField bindTextField(String fieldLabel, String fieldName)
	{
		TextField field = bindTextField(form, group, fieldLabel, fieldName);
		this.fieldList.add(field);
		return field;
	}

	public <T extends CustomField<?>> T doBinding(String fieldLabel, SingularAttribute<?, ?> field, T customField)
	{

		doBinding(group, field.getName(), customField);
		this.fieldList.add(customField);
		form.addComponent(customField);
		return customField;

	}

	public <T extends CustomField<?>> T doBinding(String fieldLabel, SetAttribute<?, ?> field, T customField)
	{

		doBinding(group, field.getName(), customField);
		this.fieldList.add(customField);
		form.addComponent(customField);
		return customField;

	}

	public <T extends CustomField<?>> T doBinding(String fieldLabel, ListAttribute<?, ?> field, T customField)
	{

		doBinding(group, field.getName(), customField);
		this.fieldList.add(customField);
		form.addComponent(customField);
		return customField;

	}

	public TextField bindTextField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName)
	{
		TextField field = new SplitTextField(fieldLabel);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		field.setNullSettingAllowed(false);
		field.setId(fieldLabel);
		addValueChangeListeners(field);
		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}

	public TextField bindTextFieldWithButton(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldLabel,
			String fieldName, Button button)
	{

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		
		TextField field = new SplitTextField(fieldLabel);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		field.setNullSettingAllowed(false);
		field.setId(fieldLabel);
		addValueChangeListeners(field);
		doBinding(group, fieldName, field);

		layout.addComponent(field);
		layout.addComponent(button);

		layout.setExpandRatio(field, 2);
		
		form.addComponent(layout);

		return field;
	}

	public void doBinding(FieldGroup group, String fieldName, @SuppressWarnings("rawtypes") Field field)
	{
		if (group != null)
			group.bind(field, fieldName);
		else

		{
			logger.warn("field {}  was not bound", fieldName);
		}
	}

	public <M> PasswordField bindPasswordField(AbstractLayout form, FieldGroup group, String fieldLabel,
			SingularAttribute<E, M> member)
	{
		PasswordField field = bindPasswordField(form, group, fieldLabel, (member != null ? member.getName() : null));
		this.fieldList.add(field);
		return field;
	}

	public <M> PasswordField bindPasswordField(String fieldLabel, SingularAttribute<E, M> member)
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
		field.setId(fieldLabel);
		addValueChangeListeners(field);
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

	public TextArea bindTextAreaField(String fieldLabel, SingularAttribute<E, String> attribute, int rows)
	{
		TextArea field = bindTextAreaField(form, group, fieldLabel, attribute.getName(), rows);
		this.fieldList.add(field);
		return field;
	}

	public TextArea bindTextAreaField(String fieldLabel, String fieldName, int rows)
	{
		TextArea field = bindTextAreaField(form, group, fieldLabel, fieldName, rows);
		this.fieldList.add(field);
		return field;
	}

	public TextArea bindTextAreaField(String fieldLabel, String fieldName, int rows, int maxlength)
	{
		TextArea field = bindTextAreaField(form, group, fieldLabel, fieldName, rows);
		field.setMaxLength(maxlength);
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
		addValueChangeListeners(field);
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

	public DateField bindDateField(String label, SingularAttribute<E, Date> member, String dateFormat,
			Resolution resolution)
	{
		DateField field = bindDateField(form, group, label, member.getName(), dateFormat, resolution);
		field.setWidth(STANDARD_COMBO_WIDTH);
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
		addValueChangeListeners(field);
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
		field.setTextInputAllowed(true);
		field.setWidth(STANDARD_COMBO_WIDTH);
		field.setImmediate(true);
		field.setId(fieldLabel);
		addValueChangeListeners(field);
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
		addValueChangeListeners(field);
		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}

	public ColorPickerField bindColorPickerField(AbstractLayout form, ValidatingFieldGroup<E> group,
			iColorFactory factory, String fieldLabel, SingularAttribute<E, iColor> member)
	{
		ColorPickerField field = bindColorPickerField(form, group, factory, fieldLabel, member.getName());
		this.fieldList.add(field);
		return field;

	}

	public ColorPickerField bindColorPickerField(AbstractLayout form, ValidatingFieldGroup<E> group,
			iColorFactory factory, String fieldLabel, String fieldName)
	{
		ColorPickerField field = new SplitColorPicker(factory, fieldLabel);
		field.setWidth("100%");
		field.setImmediate(true);

		doBinding(group, fieldName, field);

		form.addComponent(field);
		return field;
	}

	public <L> ComboBox bindComboBox(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup, String fieldName,
			String fieldLabel, Collection<?> options)
	{
		ComboBox field = new SplitComboBox(fieldLabel, options);
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(true);
		field.setWidth(STANDARD_COMBO_WIDTH);
		field.setImmediate(true);
		form.addComponent(field);
		addValueChangeListeners(field);
		doBinding(group, fieldName, field);
		return field;
	}

	public <L extends CrudEntity, K> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, L> fieldName,
			SingularAttribute<L, K> listFieldName)
	{
		return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListFieldName(listFieldName)
				.build();

	}

	public <L extends CrudEntity, K> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, L> fieldName,
			SingularAttribute<L, K> listFieldName, boolean useLazyContainer)
	{

		EntityContainer<L> container = JpaBaseDao.getGenericDao(fieldName.getJavaType()).createLazyQueryContainer();

		return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListFieldName(listFieldName)
				.setContainer(container).build();

	}

	/**
	 * Deprecated - use EntityFieldBuilder instead
	 */
	@Deprecated
	public <L extends CrudEntity> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, L> fieldName,
			Class<L> listClazz, SingularAttribute<L, ?> listFieldName)
	{
		return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListFieldName(listFieldName)
				.build();

	}

	@Deprecated
	public <L extends CrudEntity> ComboBox bindEntityField(String fieldLabel, String fieldName, Class<L> listClazz,
			String listFieldName)
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
	public <L extends CrudEntity> ComboBox bindEntityField(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup,
			String fieldLabel, SingularAttribute<E, L> field, Class<L> listClazz, SingularAttribute<L, ?> listFieldName)
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
	public <L extends CrudEntity> ComboBox bindEntityField(AbstractLayout form, ValidatingFieldGroup<E> fieldGroup,
			String fieldLabel, String fieldName, Class<L> listClazz, String listFieldName)
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
	public <L extends CrudEntity> ComboBox bindEntityField(ComboBox field, AbstractLayout form,
			ValidatingFieldGroup<E> fieldGroup, String fieldLabel, String fieldName, Class<L> listClazz,
			String listFieldName)
	{
		return new EntityFieldBuilder<L>().setComponent(field).setForm(form).setLabel(fieldLabel).setField(fieldName)
				.setListClass(listClazz).setListFieldName(listFieldName).build();

	}

	/**
	 * use this syntax to instance the builder:<br>
	 * formHelper.new EntityFieldBuilder<{name of list class}>(); <br>
	 * <br>
	 * for example<br>
	 * <br>
	 * 
	 * FormHelper&lt;RaffleBook&gt; helper = new
	 * FormHelper&lt;RaffleBook&gt;(...);<br>
	 * <br>
	 * ComboBox field = helper.new EntityFieldBuilder&lt;RaffleAllocation&gt;()<br>
	 * .setLabel("Action")<br>
	 * .setField(RaffleBook.allocation)<br>
	 * .setListFieldName(RaffleAllocation_.name)<br>
	 * .build();<br>
	 * 
	 * @author rsutton
	 * 
	 * @param <L>
	 *            the type of the list class
	 */
	public class EntityFieldBuilder<L extends CrudEntity>
	{

		private ComboBox component = null;
		private String label = null;
		private Container container = null;
		private Class<L> listClazz;
		private String listField;
		// private ValidatingFieldGroup<E> fieldGroup;
		private String field;
		private AbstractLayout builderForm;

		public ComboBox build()
		{
			Preconditions.checkNotNull(label, "Label may not be null");
			Preconditions.checkNotNull(listField, "ListField may not be null");
			Preconditions.checkArgument(group == null || field != null, "Field may not be null");
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
			component.setId(label);

			if (container == null)
			{
				Preconditions.checkNotNull(listClazz, "listClazz may not be null");
				container = JpaBaseDao.getGenericDao(listClazz).createVaadinContainer();

			}

			// Preconditions.checkState(container.getContainerPropertyIds().contains(listField),
			// listField
			// + " is not valid, valid listFieldNames are " +
			// container.getContainerPropertyIds().toString());

			ContainerAdaptor<L> adaptor = ContainerAdaptorFactory.getAdaptor(container);
			if (adaptor.getSortableContainerPropertyIds().contains(listField))
				adaptor.sort(new String[]
				{ listField }, new boolean[]
				{ true });

			component.setItemCaptionPropertyId(listField);
			component.setContainerDataSource(container);
			SingleSelectConverter<L> converter = new SingleSelectConverter<L>(component);
			component.setConverter(converter);
			component.setNewItemsAllowed(false);
			component.setNullSelectionAllowed(false);
			component.setTextInputAllowed(true);
			component.setWidth(STANDARD_COMBO_WIDTH);
			component.setImmediate(true);
			addValueChangeListeners(component);
			if (group != null)
			{
				Collection<? extends Object> ids = null;
				if (group.getContainer() != null)

					ids = group.getContainer().getContainerPropertyIds();
				else if (group.getItemDataSource() != null)
					ids = group.getItemDataSource().getItemPropertyIds();

				Preconditions
						.checkNotNull(ids, "The group must have either a Container or an ItemDataSource attached.");

				Preconditions.checkState(ids.contains(field),
						field + " is not valid, valid listFieldNames are " + ids.toString());

				doBinding(group, field, component);
			}
			builderForm.addComponent(component);
			return component;
		}

		public EntityFieldBuilder<L> useLazyContainer()
		{
			container = new JpaBaseDao<L, Long>(listClazz).createLazyQueryContainer();
			return this;
		}

		public EntityFieldBuilder<L> setContainer(JPAContainer<L> container)
		{
			this.container = container;
			return this;
		}

		public EntityFieldBuilder<L> setContainer(EntityContainer<L> container)
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

		public EntityFieldBuilder<L> setField(String field, Class<L> listClazz)
		{
			this.field = field;
			this.listClazz = listClazz;
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
			Preconditions
					.checkState(
							this.listClazz == null,
							"As you have set the field as a singularAttribute, the listClass is set automatically so there is no need to call setListClass.");
			this.listClazz = listClazz;
			return this;
		}

		@SuppressWarnings("unchecked")
		public JPAContainer<L> getContainer()
		{
			return (JPAContainer<L>) container;
		}

	}

	/**
	 * use this syntax to instance the builder:<br>
	 * formHelper.new EntityFieldBuilder<{name of list class}>(); <br>
	 * <br>
	 * for example<br>
	 * <br>
	 * 
	 * FormHelper<TblAdvertisementSize> helper = new
	 * FormHelper<TblAdvertisementSize>(...);<br>
	 * <br>
	 * ListSelect sections = helper.new ListSelectBuilder<TblSection>()<br>
	 * .setLabel("Sections")<br>
	 * .setField(TblAdvertisementSize_.tblSections)<br>
	 * .setListFieldName("name")<br>
	 * .setMultiSelect(true)<br>
	 * .build(); <br>
	 * 
	 * @author bhorvath
	 * 
	 * @param <L>
	 *            the type of the list class
	 */
	public class ListSelectBuilder<L>
	{
		private SplitListSelect component = null;
		private String label = null;
		private JPAContainer<L> container = null;
		private Class<L> listClazz;
		private String listField;
		private String field;
		private AbstractLayout builderForm;
		private boolean multiSelect = false;

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public SplitListSelect build()
		{
			Preconditions.checkNotNull(label, "label may not be null");
			Preconditions.checkNotNull(listField, "colField Property may not be null");
			if (builderForm == null)
			{
				builderForm = form;
			}
			Preconditions.checkNotNull(builderForm, "Form may not be null");

			if (component == null)
			{
				component = new SplitListSelect(label);
			}

			component.setCaption(label);
			component.setItemCaptionMode(ItemCaptionMode.PROPERTY);
			component.setItemCaptionPropertyId(listField);

			if (container == null)
			{
				Preconditions.checkNotNull(listClazz, "listClazz may not be null");
				container = JpaBaseDao.getGenericDao(listClazz).createVaadinContainer();
			}

			Preconditions.checkState(container.getContainerPropertyIds().contains(listField), listField
					+ " is not valid, valid listFields are " + container.getContainerPropertyIds().toString());

			if (container.getSortableContainerPropertyIds().contains(listField))
				container.sort(new String[]
				{ listField }, new boolean[]
				{ true });

			component.setContainerDataSource(container);

			if (this.multiSelect == true)
			{
				component.setConverter(new MultiSelectConverter(component, Set.class));
				component.setMultiSelect(true);
			}
			else
			{
				SingleSelectConverter<L> converter = new SingleSelectConverter<L>(component);
				component.setConverter(converter);
			}

			component.setWidth("100%");
			component.setImmediate(true);
			component.setNullSelectionAllowed(false);
			component.setId(label);

			if (group != null && field != null)
			{
				Preconditions.checkState(group.getContainer().getContainerPropertyIds().contains(field), field
						+ " is not valid, valid listFieldNames are "
						+ group.getContainer().getContainerPropertyIds().toString());

				doBinding(group, field, component);
			}
			builderForm.addComponent(component);

			return component;
		}

		public ListSelectBuilder<L> setMultiSelect(boolean multiSelect)
		{
			this.multiSelect = multiSelect;
			return this;
		}

		public ListSelectBuilder<L> setLabel(String label)
		{
			this.label = label;
			return this;
		}

		public ListSelectBuilder<L> setField(SingularAttribute<E, L> field)
		{
			this.field = field.getName();
			listClazz = field.getBindableJavaType();
			return this;
		}

		public ListSelectBuilder<L> setField(SetAttribute<E, L> field)
		{
			this.field = field.getName();
			listClazz = field.getBindableJavaType();
			return this;
		}

		/**
		 * Sets the field to display from the List entity.
		 * 
		 * @param colField
		 * @return
		 */
		public ListSelectBuilder<L> setListFieldName(SingularAttribute<L, ?> colField)
		{
			this.listField = colField.getName();
			return this;
		}

		public ListSelectBuilder<L> setListFieldName(String colField)
		{
			this.listField = colField;
			return this;
		}

		public ListSelectBuilder<L> setContainer(JPAContainer<L> container)
		{
			this.container = container;
			return this;
		}

		public ListSelectBuilder<L> setForm(AbstractLayout form)
		{
			this.builderForm = form;
			return this;
		}

		public ListSelectBuilder<L> setComponent(SplitListSelect component)
		{
			this.component = component;
			return this;
		}

		public ListSelectBuilder<L> setListClass(Class<L> listClazz)
		{
			Preconditions.checkState(this.listClazz == null,
					"If you set the field as a singularAttribute, the listClass is set automatically.");
			this.listClazz = listClazz;
			return this;
		}

	}

	/**
	 * use this syntax to instance the builder:<br>
	 * The formhelper must be an Entity with an attribute containing a Set of
	 * items that will be selected.
	 * 
	 * formHelper.new EntityFieldBuilder<{class name of the items in the
	 * list}>(); <br>
	 * <br>
	 * If you need to filter the set of available items then you must explicity
	 * set the list container and filter it.
	 * 
	 * for example<br>
	 * <br>
	 * 
	 * FormHelper<TblAdvertisementSize> helper = new
	 * FormHelper<TblAdvertisementSize>(...);<br>
	 * <br>
	 * TwinColSelect sections = helper.new TwinColSelectBuilder<TblSection>()<br>
	 * .setLabel("Sections")<br>
	 * .setField(TblAdvertisementSize_.tblSections)<br>
	 * .setListFieldName("name")<br>
	 * .setLeftColumnCaption("Available") .setRightColumnCaption("Selected")
	 * .build(); <br>
	 * 
	 * @author bhorvath
	 * 
	 * @param <L>
	 *            the type of the list class
	 */
	public class TwinColSelectBuilder<L extends CrudEntity>
	{
		private SplitTwinColSelect component = null;
		private String label = null;
		private Container container = null;
		private Class<L> listClazz;
		private String listField;
		private String field;
		private AbstractLayout builderForm;
		private String leftColumnCaption = "Available";
		private String rightColumnCaption = "Selected";

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public SplitTwinColSelect build()
		{
			Preconditions.checkNotNull(label, "label may not be null");
			Preconditions.checkNotNull(listField, "colField Property may not be null");
			Preconditions.checkArgument(group == null || field != null, "Field may not be null");
			if (builderForm == null)
			{
				builderForm = form;
			}
			Preconditions.checkNotNull(builderForm, "Form may not be null");

			if (component == null)
			{
				component = new SplitTwinColSelect(label);
			}

			component.setCaption(label);
			component.setItemCaptionMode(ItemCaptionMode.PROPERTY);
			component.setItemCaptionPropertyId(listField);
			component.setLeftColumnCaption(leftColumnCaption);
			component.setRightColumnCaption(rightColumnCaption);

			if (container == null)
			{
				Preconditions.checkNotNull(listClazz, "listClazz may not be null");
				container = JpaBaseDao.getGenericDao(listClazz).createVaadinContainer();
			}

			Preconditions.checkState(container.getContainerPropertyIds().contains(listField), listField
					+ " is not valid, valid listFields are " + container.getContainerPropertyIds().toString());

			ContainerAdaptor<L> adaptor = ContainerAdaptorFactory.getAdaptor(container);
			if (adaptor.getSortableContainerPropertyIds().contains(listField))
				adaptor.sort(new String[]
				{ listField }, new boolean[]
				{ true });

			component.setContainerDataSource(container);
			component.setConverter(new MultiSelectConverter(component, Set.class));

			component.setWidth("100%");
			component.setId(label);
			component.setImmediate(true);
			component.setNullSelectionAllowed(true);
			component.setBuffered(true);
			addValueChangeListeners(component);

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

		/**
		 * label that will appear next to the component on the screen
		 * 
		 * @param label
		 * @return
		 */
		public TwinColSelectBuilder<L> setLabel(String label)
		{
			this.label = label;
			return this;
		}

		public TwinColSelectBuilder<L> setLeftColumnCaption(String leftColumnCaption)
		{
			this.leftColumnCaption = leftColumnCaption;
			return this;
		}

		public TwinColSelectBuilder<L> setRightColumnCaption(String rightColumnCaption)
		{
			this.rightColumnCaption = rightColumnCaption;
			return this;
		}

		/**
		 * the set in the parent table that holds the set of children
		 * 
		 * @param field
		 * @return
		 */
		public TwinColSelectBuilder<L> setField(SetAttribute<E, L> field)
		{
			this.field = field.getName();
			listClazz = field.getBindableJavaType();
			return this;
		}

		public TwinColSelectBuilder<L> setListFieldName(SingularAttribute<L, ?> colField)
		{
			this.listField = colField.getName();
			return this;
		}

		public TwinColSelectBuilder<L> setListFieldName(String colField)
		{
			this.listField = colField;
			return this;
		}

		/**
		 * Set the list container of available items. A container will normally
		 * be generated automatically based on the List class <L>. However if
		 * you need to filter the list of available items you will need to
		 * provide your own container which is filtered.
		 * 
		 * @param container
		 * @return
		 */
		public TwinColSelectBuilder<L> setContainer(JPAContainer<L> container)
		{
			this.container = container;
			return this;
		}

		public TwinColSelectBuilder<L> setContainer(EntityContainer<L> container)
		{
			this.container = container;
			return this;
		}

		public TwinColSelectBuilder<L> setForm(AbstractLayout form)
		{
			this.builderForm = form;
			return this;
		}

		public TwinColSelectBuilder<L> setComponent(SplitTwinColSelect component)
		{
			this.component = component;
			return this;
		}

		public TwinColSelectBuilder<L> setListClass(Class<L> listClazz)
		{
			Preconditions.checkState(this.listClazz == null,
					"If you set the field as a singularAttribute, the listClass is set automatically.");
			this.listClazz = listClazz;
			return this;
		}

		public TwinColSelectBuilder<L> useLazyContainer()
		{
			container = new JpaBaseDao<L, Long>(listClazz).createLazyQueryContainer();
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
		addValueChangeListeners(field);
		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}

	// public <L> EntityAutoCompleteField
	// bindAutoCompleteField(AutoCompleteParent<E> parent,
	// String fieldLabel, ListAttribute<E, L> entityField, Class<L> listClazz)
	// {
	// // hack
	// ContactDao dao = new DaoFactory().getContactDao();
	// container = dao.createVaadinContainer();
	//
	// //EntityAutoCompleteField<CrudEntity, JpaBaseDao<E,Long>> field = new
	// EntityAutoCompleteField<CrudEntity, JpaBaseDao<E,Long>>(container, dao,
	// fieldLabel, parent);
	// EntityAutoCompleteField field = new EntityAutoCompleteField<CrudEntity,
	// JpaBaseDao(container, dao, fieldLabel, parent);
	// return field;
	//
	// }

	// public <L> EntityAutoCompleteField
	// bindAutoCompleteField(AutoCompleteParent<E> parent,
	// String fieldLabel, ListAttribute<E, L> entityField, Class<L> listClazz)
	// {
	// // hack
	// ContactDao dao = new DaoFactory().getContactDao();
	// container = dao.createVaadinContainer();
	//
	// //EntityAutoCompleteField<CrudEntity, JpaBaseDao<E,Long>> field = new
	// EntityAutoCompleteField<CrudEntity, JpaBaseDao<E,Long>>(container, dao,
	// fieldLabel, parent);
	// EntityAutoCompleteField field = new EntityAutoCompleteField<CrudEntity,
	// JpaBaseDao(container, dao, fieldLabel, parent);
	// return field;
	//
	// }

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

	static public <Q extends CrudEntity> Container createContainerFromEntities(String fieldName, Collection<Q> list)
	{
		LinkedHashMap<Q, String> enumMap = new LinkedHashMap<Q, String>();

		List<Q> sortedList = new LinkedList<>();
		sortedList.addAll(list);
		Collections.sort(sortedList, new Comparator<Q>()
		{
			@Override
			public int compare(Q arg0, Q arg1)
			{
				return arg0.getName().compareToIgnoreCase(arg1.getName());
			}
		});

		for (Q value : sortedList)
		{
			enumMap.put(value, value.getName());
		}

		return createContainerFromMap(fieldName, enumMap);
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
		logger.error(sb.toString());
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

	public CKEditorEmailField bindEditorField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldName,
			boolean readonly, ConfigModifier configModifier)
	{
		SplitEditorField field = new SplitEditorField(readonly, configModifier);
		field.setWidth("100%");
		field.setImmediate(true);
		addValueChangeListeners(field);
		doBinding(group, fieldName, field);
		form.addComponent(field);
		return field;
	}

	public <EN> ComboBox bindEnumField(String fieldLabel, SingularAttribute<E, EN> fieldName)
	{
		return bindEnumField(fieldLabel, fieldName.getName(), fieldName.getBindableJavaType());

	}

	public <J extends CrudEntity> FormHelper<E>.EntityFieldBuilder<J> getEntityFieldBuilder(Class<J> j)
	{
		return new EntityFieldBuilder<J>();

	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	static public <J extends CrudEntity> FormHelper<?>.ListSelectBuilder<J> getListSelectBuilder(AbstractLayout form,
			Class<J> j)
	{
		FormHelper<?> helper = new FormHelper(form, null);
		return helper.new ListSelectBuilder<J>().setListClass(j);

	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	static public <J extends CrudEntity> FormHelper<?>.EntityFieldBuilder<J> getEntityFieldBuilder(AbstractLayout form,
			Class<J> j)
	{
		FormHelper<?> helper = new FormHelper(form, null);
		return helper.new EntityFieldBuilder<J>().setListClass(j);

	}

	public <J extends CrudEntity> FormHelper<E>.TwinColSelectBuilder<J> getTwinColSelectBuilder(Class<J> j)
	{
		return new TwinColSelectBuilder<J>();
	}

	public void addComponent(Component component)
	{
		form.addComponent(component);

	}

}
