package au.com.vaadinutils.crud;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;

import au.com.vaadinutils.crud.splitFields.SplitField;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
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
import com.vaadin.ui.VerticalLayout;

public class MultiColumnFormLayout<E> extends VerticalLayout
{
	private static Logger logger = Logger.getLogger(MultiColumnFormLayout.class);
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

	final private GridLayout grid;

	int x = 0;
	int y = 0;

	public MultiColumnFormLayout(int columns, ValidatingFieldGroup<E> fieldGroup)
	{
		//super.setDescription("MultiColumnFormLayout");
		this.columns = columns * 2;

		this.labelWidths = new int[columns];
		this.fieldWidths = new int[columns];
		for (int i = 0; i < columns; i++)
		{
			this.labelWidths[i] = DEFAULT_LABEL_WIDTH;
			this.fieldWidths[i] = DEFAULT_FIELD_WIDTH;
		}

		grid = new GridLayout(columns * 2, 1);
		// grid.setDescription("Grid within MultiColumnLayout");
		grid.setSizeFull();
		grid.setSpacing(true);

		formHelper = getFormHelper(grid, fieldGroup);
		init();
		this.fieldGroup = fieldGroup;
		this.setSizeFull();
		super.addComponent(grid);

		for (int i = 1; i < columns * 2; i += 2)
		{
			grid.setColumnExpandRatio(i, 1.0f);
		}

	}
	
	protected FormHelper<E> getFormHelper(GridLayout grid2, ValidatingFieldGroup<E> fieldGroup2)
	{
		return new FormHelper<E>(grid, fieldGroup);
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
	 * Sets the width of the labels in the given column.
	 * 
	 * @param column
	 *            - zero based column to set the width of
	 * @param width
	 *            - the width to set all labels to.
	 */
	public void setColumnFieldWidth(int column, int width)
	{
		this.fieldWidths[column] = width;
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
			grid.addComponent(component);
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

		int fieldSpan = colspan;
		int captionWidth = 1;
		if (x + fieldSpan + captionWidth > columns)
		{
			x = 0;
			y++;
			grid.insertRow(y);

		}
		int labelWidth = this.labelWidths[x / 2];
		Label caption;
		if (splitComponent.getCaption() == null || splitComponent.getCaption().length() == 0)
			caption = new Label("");
		else
			caption = splitComponent.getLabel();
		caption.setWidth("" + labelWidth);
		logger.warn("label: caption:" + caption + " width:" + labelWidth + " x:" + x + " y:" + y + " for col:" + x / 2);
		grid.addComponent(caption, x, y, x, y);
		grid.setComponentAlignment(caption, Alignment.MIDDLE_RIGHT);
		x++;

		String fieldWidth = getFieldWidth(x, fieldSpan);
		logger.warn("field:" + caption + " width: " + fieldWidth + " X:" + x + " Y:" + y + " X1:"
				+ (x + fieldSpan - 1) + " Y1:" + y);
		splitComponent.setWidth(fieldWidth);

		grid.addComponent(splitComponent, x, y, x + fieldSpan - 1, y);
		x += fieldSpan;

		grid.setComponentAlignment(splitComponent, Alignment.MIDDLE_LEFT);

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
		grid.insertRow(grid.getRows());
		grid.newLine();
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

	/**
	 * Adds a text field to the form without binding it to the FieldGroup
	 * 
	 * @param caption
	 * @return
	 */
	public TextField addTextField(String caption)
	{
		TextField field = new TextField(caption);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		field.setNullSettingAllowed(false);
		this.addComponent(field);
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
	public PasswordField addPasswordField(String caption)
	{
		PasswordField field = new PasswordField(caption);
		field.setWidth("100%");
		field.setImmediate(true);
		field.setNullRepresentation("");
		field.setNullSettingAllowed(false);
		this.addComponent(field);
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
	

	public DateField bindDateField(String fieldLabel, SingularAttribute<E, Date> dateField, String dateFormat,
			Resolution resolution)
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

	public void setEntityManager(EntityManagerFactory factory)
	{
		FormHelper.setEntityManagerFactory(factory);
	}

	/**
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
	public <L> ComboBox bindEntityField(String fieldLabel, String fieldName, Class<L> listClazz, String listFieldName)
	{
		Preconditions.checkArgument(formHelper.isEntitymanagerSet(),
				"You must provide the entity manager factory by calling setEntityManager first.");

		ComboBox field = formHelper.bindEntityField(this, fieldGroup, fieldLabel, fieldName, listClazz, listFieldName);
		this.fieldList.add(field);
		return field;
	}
	
	
	public <F, L, M> ComboBox bindEntityField(String fieldLabel, SingularAttribute<E, F> fieldName, Class<L> listClazz,
			SingularAttribute<L, M> listFieldName)
	{
		Preconditions.checkArgument(formHelper.isEntitymanagerSet(),
				"You must provide the entity manager factory by calling setEntityManager first.");

		ComboBox field = formHelper.bindEntityField(this, fieldGroup, fieldLabel, fieldName, listClazz, listFieldName);
		this.fieldList.add(field);
		return field;
	}
	
	public ComboBox bindComboBox(String fieldLabel,Collection<?> options)
	{
		ComboBox field = formHelper.bindComboBox(this, fieldGroup, fieldLabel, options);
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


	protected FormHelper<E> getFormHelper()
	{
		return this.formHelper;
	}
}
