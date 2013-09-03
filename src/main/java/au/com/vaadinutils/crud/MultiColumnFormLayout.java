package au.com.vaadinutils.crud;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import au.com.vaadinutils.crud.splitFields.SplitField;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.data.fieldgroup.FieldGroup;
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
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MultiColumnFormLayout.class);
	private static final long serialVersionUID = 1L;
	private final int columns;
	private int colspan = 1;

	private ValidatingFieldGroup<E> fieldGroup;
	private ArrayList<AbstractComponent> fieldList = new ArrayList<AbstractComponent>();
	private FormHelper<E> formHelper;

	final private GridLayout grid;

	int x = 0;
	int y = 0;

	public MultiColumnFormLayout(int columns, ValidatingFieldGroup<E> fieldGroup)
	{
		grid = new GridLayout(columns * 2, 1);
		this.columns = columns * 2;
		formHelper = new FormHelper<E>(grid, fieldGroup);
		init();
		this.fieldGroup = fieldGroup;
		this.setSizeFull();
		super.addComponent(grid);

		VerticalLayout filler = new VerticalLayout();
		filler.setSizeFull();
		super.addComponent(filler);
		this.setExpandRatio(filler, 1.0f);
		grid.setSpacing(true);

		for (int i = 1; i < columns * 2; i += 2)
		{
			grid.setColumnExpandRatio(i, 1.0f / new Float(columns));
		}

	}

	private void init()
	{
		super.setSpacing(true);
		super.setMargin(true);
	}

	@Override
	public void addComponent(Component component)
	{
		Preconditions.checkArgument(component instanceof SplitField);
		internalAddComponent((SplitField) component);
	}

	/**
	 * Add a component to the grid. If colspan has been set then it is honoured.
	 * If we are at the end of the row then automatically wrap this item to the
	 * end of the next row.
	 */

	public void internalAddComponent(SplitField splitComponent)
	{
		// SplitField splitComponent = (SplitField) component;

		int fieldWidth = colspan;
		int captionWidth = 0;
		if (splitComponent.getCaption()!= null && splitComponent.getCaption().length()>=0)
		{
			captionWidth = 1;
			System.out.println("'"+splitComponent.getCaption()+"'");
		}else
		{
			System.out.println("No caption");
		}
		
		
		if (x + fieldWidth+captionWidth >= columns)
		{
			x = 0;
			y++;
			grid.insertRow(y);

		}
		if (captionWidth==1)
		{
			grid.addComponent(splitComponent.getLabel(), x, y, x, y);
			grid.setComponentAlignment(splitComponent.getLabel(), Alignment.MIDDLE_RIGHT);
			x++;
		}

		

		System.out.println(splitComponent.getCaption() + " X:" + x + " Y:" + y +" X1:"+(x+fieldWidth-1)+" Y1:"+y);
		grid.addComponent(splitComponent, x , y, x + fieldWidth-1, y);
		x += fieldWidth;

		grid.setComponentAlignment(splitComponent, Alignment.MIDDLE_LEFT);

		splitComponent.setSizeFull();

		this.colspan = 1;
	}

	/**
	 * Adds a new row to the grid and moves the cursor down one row.
	 */
	public void newLine()
	{
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
	
	/** 
	 * Adds a text field to the form without binding it to the FieldGroup
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


	public PasswordField bindPasswordField(String fieldLabel, String fieldName)
	{
		PasswordField field = formHelper.bindPasswordField(this, fieldGroup, fieldLabel, fieldName);

		this.fieldList.add(field);
		return field;
	}

	/** 
	 * Adds a text field to the form without binding it to the FieldGroup
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

	public DateField bindDateField(String fieldLabel, String fieldName)
	{
		DateField field = formHelper.bindDateField(this, fieldGroup, fieldLabel, fieldName);
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

	public ComboBox bindEnumField(String fieldLabel, String fieldName, Class<?> clazz)
	{
		ComboBox field = formHelper.bindEnumField(this, fieldGroup, fieldLabel, fieldName, clazz);
		this.fieldList.add(field);
		return field;
	}

	public CheckBox bindBooleanField(String fieldLabel, String fieldName)
	{
		CheckBox field = formHelper.bindBooleanField(this, fieldGroup, fieldLabel, fieldName);
		this.fieldList.add(field);
		return field;
	}

	public void setEntityManager(EntityManagerFactory factory)
	{
		formHelper.setEntityManagerFactory(factory);
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

		ComboBox field = formHelper.bindEntityField(this, fieldGroup, fieldLabel, fieldName, listFieldName, listClazz);
		this.fieldList.add(field);
		return field;
	}

	public ArrayList<AbstractComponent> getFieldList()
	{
		return this.fieldList;
	}

	public FieldGroup getFieldGroup()
	{
		return fieldGroup;
	}

}
