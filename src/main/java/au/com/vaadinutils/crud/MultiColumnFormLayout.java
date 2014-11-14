package au.com.vaadinutils.crud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.splitFields.SplitField;
import au.com.vaadinutils.crud.splitFields.SplitLabel;
import au.com.vaadinutils.domain.iColorFactory;
import au.com.vaadinutils.fields.CKEditorEmailField;
import au.com.vaadinutils.fields.ColorPickerField;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class MultiColumnFormLayout<E extends CrudEntity> extends GridLayout
{
	private static  transient Logger logger   =  LogManager.getLogger(MultiColumnFormLayout.class);
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_LABEL_WIDTH = 120;
	private static final int DEFAULT_FIELD_WIDTH = 100;
	private final int columns;
	private int colspan = 1;

	private int labelWidths[];
	private int fieldWidths[];

	private ValidatingFieldGroup<E> fieldGroup;
	private ArrayList<AbstractComponent> fieldList = new ArrayList<AbstractComponent>();
	private FormHelper<E> formHelper;

	//final private GridLayout grid;

	int x = 0;
	int y = 0;

	public MultiColumnFormLayout(int columns, ValidatingFieldGroup<E> fieldGroup)
	{
		this.fieldGroup = fieldGroup;
		// super.setDescription("MultiColumnFormLayout");
		this.columns = columns * 2;

		this.labelWidths = new int[columns];
		this.fieldWidths = new int[columns];
		for (int i = 0; i < columns; i++)
		{
			this.labelWidths[i] = DEFAULT_LABEL_WIDTH;
			this.fieldWidths[i] = DEFAULT_FIELD_WIDTH;
		}

		this.setColumns(columns * 2);
		this.setRows(1);
		this.setSpacing(true);

		formHelper = getFormHelper(this, fieldGroup);
		init();

		for (int i = 1; i < columns * 2; i += 2)
		{
			this.setColumnExpandRatio(i, 1.0f);
		}

	}

	protected FormHelper<E> getFormHelper(MultiColumnFormLayout<E> layout, ValidatingFieldGroup<E> fieldGroup)
	{
		return new FormHelper<E>(layout, fieldGroup);
	}

	/**
	 * Sets the width of the labels in the given column.
	 * 
	 * @param column
	 *            - zero based column to set the width of
	 * @param width
	 *            - the width to set all labels to.
	 */
	public void setColumnLabelWidth(int column, int width)
	{
		this.labelWidths[column] = width;
	}

	/**
	 * Sets the width of the fields in the given column.
	 * 
	 * @param column
	 *            - zero based column to set the width of
	 * @param width
	 *            - the width to set all fields to.
	 */
	public void setColumnFieldWidth(int column, int width)
	{
		this.fieldWidths[column] = width;
	}

	public void setComponentAlignment(Component childComponent, Alignment alignment)
	{
		super.setComponentAlignment(childComponent, alignment);
	}

	private void init()
	{
		super.setSpacing(true);
		super.setMargin(true);
	}

	@Override
	public void addComponent(Component component)
	{
		if (component instanceof SplitField)
		{
			internalAddComponent((SplitField) component);
		}
		else
		{
			super.addComponent(component);
			x++;
			if (x > columns)
			{
				x = 0;
				y++;
				super.insertRow(y);
			}
		}
	}

	/**
	 * Add a component to the grid. If colspan has been set then it is honoured.
	 * If we are at the end of the row then automatically wrap this item to the
	 * end of the next row.
	 */

	public void internalAddComponent(SplitField splitComponent)
	{
		// SplitField splitComponent = (SplitField) component;

		splitComponent.hideLabel();
		int fieldSpan = colspan;
		int captionWidth = 1;
		if (x + fieldSpan + captionWidth > columns)
		{
			x = 0;
			y++;
			super.insertRow(y);
		}
		int labelWidth = this.labelWidths[x / 2];
		Label caption;
		if (splitComponent.getCaption() == null || splitComponent.getCaption().length() == 0)
			caption = new Label("");
		else
			caption = splitComponent.getLabel();
		caption.setWidth("" + labelWidth);
		logger.debug("label: caption: {}  width: {}  x: {} y: {} for col: {}", caption.getValue(), labelWidth, x, y,
				x / 2);

		super.addComponent(caption, x, y, x, y);
		super.setComponentAlignment(caption, Alignment.MIDDLE_RIGHT);
		x++;

		String fieldWidth = getFieldWidth(x, fieldSpan);
		logger.debug("field: {} width: {} X: {} Y: {} X1: {} Y1: {}", caption.getValue(), fieldWidth, x, y, (x
				+ fieldSpan - 1), y);
		splitComponent.setWidth(fieldWidth);

		if (!(splitComponent instanceof SplitLabel))
		{
			super.addComponent(splitComponent, x, y, x + fieldSpan - 1, y);
			super.setComponentAlignment(splitComponent, Alignment.MIDDLE_LEFT);
		}

		x += fieldSpan;

		this.colspan = 1;
	}

	private String getFieldWidth(int x, int fieldSpan)
	{
		int column = x / 2;
		int width = 0;

		width = fieldWidths[column];

		for (int i = 1; i < fieldSpan; i++)
		{
			width += labelWidths[column + i];
			width += fieldWidths[column + i];
		}
		return "" + width;
	}

	/**
	 * Adds a new row to the grid and moves the cursor down one row.
	 */
	public void newLine()
	{
		x = 0;
		y++;
		super.insertRow(super.getRows());
		super.newLine();
	}

	/**
	 * Set the colspan for the next component that is inserted after which the
	 * colspan will be reset to 1.
	 * 
	 * @param colspan
	 */
	public void colspan(int colspan)
	{
		this.colspan = colspan;

	}

	/**
	 * 
	 * @param fieldLabel
	 *            - the label that will be displayed in the screen layout
	 * @param fieldName
	 *            - the column name of the field in the database
	 * @return
	 */
	public TextField bindTextField(String fieldLabel, String fieldName)
	{
		TextField field = formHelper.bindTextField(this, fieldGroup, fieldLabel, fieldName);

		this.fieldList.add(field);
		return field;
	}

	public <M> TextField bindTextField(String fieldLabel, SingularAttribute<E, M> member)
	{
		TextField field = formHelper.bindTextField(this, fieldGroup, fieldLabel, member);
		this.fieldList.add(field);
		return field;
	}

	public CKEditorEmailField bindEditorField(SingularAttribute<E, String> member, boolean readonly)
	{
		CKEditorEmailField field = formHelper.bindEditorField(this, fieldGroup, member, readonly);
		this.fieldList.add(field);
		return field;

	}

	/**
	 * Adds a text field to the form without binding it to the FieldGroup
	 * 
	 * @param caption
	 * @return
	 */
	public TextField addTextField(String fieldLabel)
	{
		TextField field = formHelper.bindTextField(this, (ValidatingFieldGroup<E>) null, fieldLabel,
				(String) null);

		this.fieldList.add(field);
		return field;
	}

	public PasswordField bindPasswordField(String fieldLabel, SingularAttribute<E, String> fieldName)
	{
		PasswordField field = formHelper.bindPasswordField(this, fieldGroup, fieldLabel, fieldName);

		this.fieldList.add(field);
		return field;
	}

	public PasswordField bindPasswordField(String fieldLabel, String fieldName)
	{
		PasswordField field = formHelper.bindPasswordField(this, fieldGroup, fieldLabel, fieldName);

		this.fieldList.add(field);
		return field;
	}

	/**
	 * Adds a text field to the form without binding it to the FieldGroup
	 * 
	 * @param caption
	 * @return
	 */
	public PasswordField addPasswordField(String fieldLabel)
	{
		PasswordField field = formHelper.bindPasswordField(this, (ValidatingFieldGroup<?>) null, fieldLabel,
				(SingularAttribute<E, String>) null);
		this.fieldList.add(field);
		return field;
	}

	public <M> TextArea bindTextAreaField(String fieldLabel, SingularAttribute<E, M> member, int rows)
	{
		TextArea field = formHelper.bindTextAreaField(this, fieldGroup, fieldLabel, member, rows);
		this.fieldList.add(field);
		return field;
	}

	/**
	 * 
	 * @param fieldLabel
	 *            - the label that will be displayed in the screen layout
	 * @param fieldName
	 *            - the column name of the field in the database
	 * @return
	 */
	public TextArea bindTextAreaField(String fieldLabel, String fieldName, int rows)
	{
		TextArea field = formHelper.bindTextAreaField(this, fieldGroup, fieldLabel, fieldName, rows);
		this.fieldList.add(field);
		return field;
	}

	public DateField bindDateField(String fieldLabel, SingularAttribute<E, ? extends Date> dateField,
			String dateFormat, Resolution resolution)
	{
		DateField field = formHelper.bindDateField(this, fieldGroup, fieldLabel, dateField, dateFormat, resolution);
		this.fieldList.add(field);
		return field;
	}

	public DateField bindDateField(String fieldLabel, String fieldName, String dateFormat, Resolution resolution)
	{
		DateField field = formHelper.bindDateField(this, fieldGroup, fieldLabel, fieldName, dateFormat, resolution);
		this.fieldList.add(field);
		return field;
	}

	public Label bindLabel(String fieldLabel)
	{
		Label field = formHelper.bindLabel(this, fieldGroup, fieldLabel);
		this.fieldList.add(field);
		return field;
	}

	public Label bindLabel(Label label)
	{
		Label field = formHelper.bindLabel(this, fieldGroup, label);
		this.fieldList.add(field);
		return field;
	}

	public <M> ComboBox bindEnumField(String fieldLabel, SingularAttribute<E, M> member, Class<?> clazz)
	{
		ComboBox field = formHelper.bindEnumField(this, fieldGroup, fieldLabel, member, clazz);
		this.fieldList.add(field);
		return field;
	}

	public ComboBox bindEnumField(String fieldLabel, String fieldName, Class<?> clazz)
	{
		ComboBox field = formHelper.bindEnumField(this, fieldGroup, fieldLabel, fieldName, clazz);
		this.fieldList.add(field);
		return field;
	}

	public CheckBox bindBooleanField(String fieldLabel, SingularAttribute<E, Boolean> member)
	{
		CheckBox field = formHelper.bindBooleanField(this, fieldGroup, fieldLabel, member);
		this.fieldList.add(field);
		return field;
	}

	public CheckBox bindBooleanField(String fieldLabel, String member)
	{
		CheckBox field = formHelper.bindBooleanField(this, fieldGroup, fieldLabel, member);
		this.fieldList.add(field);
		return field;
	}

	public ColorPickerField bindColorPicker(iColorFactory factory, String fieldLabel, String member)
	{
		ColorPickerField field = formHelper.bindColorPickerField(this, fieldGroup, factory, fieldLabel, member);
		this.fieldList.add(field);
		return field;
	}

	// public ColorPickerField bindColorPicker(iColorFactory factory, String
	// fieldLabel, SingularAttribute<E, iColor> member)
	// {
	// ColorPickerField field = formHelper.bindColorPickerField(this,
	// fieldGroup, factory, fieldLabel, member);
	// this.fieldList.add(field);
	// return field;
	// }

	/**
	 * Deprecated - Use EntityFieldBuilder instead
	 * 
	 * @param fieldLabel
	 * @param fieldName
	 *            - name of primary entities member field that holds the foreign
	 *            entity
	 * @param listClazz
	 *            - Class of the foreign entity
	 * @param listFieldName
	 *            - name of the field to display in the combo box from the
	 *            foreign entity
	 * @return
	 */
	@Deprecated
	public <L extends CrudEntity> ComboBox bindEntityField(String fieldLabel, String fieldName, Class<L> listClazz, String listFieldName)
	{
		ComboBox field = formHelper.bindEntityField(this, fieldGroup, fieldLabel, fieldName, listClazz, listFieldName);
		this.fieldList.add(field);
		return field;
	}

	/**
	 * Deprecated - Use EntityFieldBuilder instead
	 * 
	 * @param fieldLabel
	 * @param fieldName
	 * @param listClazz
	 * @param listFieldName
	 * @return
	 */
	@Deprecated
	public <L extends CrudEntity> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, L> fieldName, Class<L> listClazz,
			SingularAttribute<L, ?> listFieldName)
	{
		ComboBox field = formHelper.bindEntityField(this, fieldGroup, fieldLabel, fieldName, listClazz, listFieldName);
		this.fieldList.add(field);
		return field;
	}

	public ComboBox bindComboBox(String fieldLabel, String fieldName,Collection<?> options)
	{
		ComboBox field = formHelper.bindComboBox(this, fieldGroup, fieldName,fieldLabel, options);
		this.fieldList.add(field);
		return field;
	}

	public ArrayList<AbstractComponent> getFieldList()
	{
		return this.fieldList;
	}

	public ValidatingFieldGroup<E> getFieldGroup()
	{
		return fieldGroup;
	}

	public FormHelper<E> getFormHelper()
	{
		return this.formHelper;
	}

	public void setReadOnly(boolean readOnly)
	{
		if (this.fieldGroup.getItemDataSource() != null)
			this.fieldGroup.setReadOnly(readOnly);
	}

	/**
	 * Sets the expand ratio on the row that is currently last.
	 * 
	 * @param ratio
	 */
	public void setExpandRatio(float ratio)
	{
		super.setRowExpandRatio(super.getRows() - 1, ratio);
	}

	/**
	 * Sets the given columns expand ratio.
	 * 
	 * @param columnIndex
	 * @param ratio
	 */
	public void setColumnExpandRatio(int columnIndex, float ratio)
	{
		super.setColumnExpandRatio(columnIndex, ratio);
	}
}
