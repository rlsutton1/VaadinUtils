package au.com.vaadinutils.fields;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class TableCheckBoxSingleSelect extends Table
{

	public static final String TABLE_CHECK_BOX_SELECT = "TableCheckBoxSelect";
	private static final long serialVersionUID = -7559267854874304189L;
	private Set<ValueChangeListener> valueChangeListeners = new HashSet<ValueChangeListener>();
	protected int containerSize = 0;
	private Object selectedId;

	public TableCheckBoxSingleSelect()
	{
		init();
		setImmediate(true);

	}

	/**
	 * call this method after adding your custom fields
	 */
	public void init()
	{

		this.addGeneratedColumn(TABLE_CHECK_BOX_SELECT, getGenerator());
		super.setMultiSelect(false);
		super.setSelectable(false);

	}

	private Property.ValueChangeEvent getValueChangeEvent()
	{
		Property.ValueChangeEvent event = new Property.ValueChangeEvent()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 3393822114324878273L;

			@SuppressWarnings("rawtypes")
			@Override
			public Property getProperty()
			{
				return new Property()
				{

					private static final long serialVersionUID = 8430716281101427107L;

					@Override
					public Object getValue()
					{

						return getSelectedItems();
					}

					@Override
					public void setValue(Object newValue) throws ReadOnlyException
					{
						throw new RuntimeException("Not implemented");
					}

					@Override
					public Class getType()
					{
						throw new RuntimeException("Not implemented");
					}

					@Override
					public boolean isReadOnly()
					{
						return true;
					}

					@Override
					public void setReadOnly(boolean newStatus)
					{
						throw new RuntimeException("Not implemented");
					}
				};
			}
		};
		return event;
	}

	public void setColumnHeaders(String... columnHeaders)
	{
		Set<String> cols = new LinkedHashSet<String>();
		for (String col : columnHeaders)
		{
			cols.add(col);
		}

		cols.add("");

		super.setColumnHeaders(cols.toArray(new String[] {}));

	}

	@Override
	public void setVisibleColumns(Object... visibleColumns)
	{
		if (visibleColumns.length > 0)
		{
			Set<Object> cols = new LinkedHashSet<Object>();
			for (Object col : visibleColumns)
			{
				cols.add(col);
			}

			cols.add(TABLE_CHECK_BOX_SELECT);

			super.setVisibleColumns(cols.toArray());
		}
		else
		{
			// during initialisation it comes through here empty and if we add
			// ours in npe's out
			super.setVisibleColumns(visibleColumns);
		}

	}

	/**
	 * use setSelectedValue instead, this method gets called before
	 * initialization
	 */
	@Deprecated
	public void setValue(Object value)
	{
		super.setValue(value);
	}

	public void setSelectedValue(Object value)
	{
		selectedId = value;

		this.refreshRowCache();

	}

	@Override
	public boolean isMultiSelect()
	{
		return false;
	}

	@Override
	public void setSelectable(boolean s)
	{
		throw new RuntimeException("Dont call this!");
	}

	@Override
	public boolean isSelectable()
	{
		return false;
	}

	public Object getSelectedItems()
	{
		Set<Object> tmp = new HashSet<>();
		if (selectedId != null)
		{
			tmp.add(selectedId);
		}
		return tmp;
	}

	@Override
	public void addValueChangeListener(ValueChangeListener listener)
	{
		valueChangeListeners.add(listener);
	}

	/**
	 * call getSelectedItems instead, can't use this method as Vaadins table
	 * calls back to this method on a paint cycle, showing some items as
	 * selected
	 */
	@Deprecated
	@Override
	public Object getValue()
	{

		return null;

	}

	private CheckBox selectedCheckBox;

	protected ColumnGenerator getGenerator()
	{
		return new ColumnGenerator()
		{

			private static final long serialVersionUID = -6659059346271729122L;

			@Override
			public Object generateCell(final Table source, final Object itemId, Object columnId)
			{

				final CheckBox checkbox = new CheckBox();
				checkbox.setWidth("25");
				checkbox.setHeight("20");

				checkbox.setValue(itemId == selectedId);
				if (itemId == selectedId)
				{
					selectedCheckBox = checkbox;
				}

				checkbox.addValueChangeListener(new ValueChangeListener()
				{

					private static final long serialVersionUID = 9170497247408214336L;

					@Override
					public void valueChange(Property.ValueChangeEvent event)
					{
						if (selectedCheckBox != null)
						{
							selectedCheckBox.setValue(false);
						}
						if ((Boolean) event.getProperty().getValue() == true)
						{
							selectedId = itemId;
							selectedCheckBox = checkbox;
						}
						else
						{
							selectedCheckBox = null;
							selectedId = null;
						}
						notifyValueChange();
					}

				});
				checkbox.setImmediate(true);

				return checkbox;

			}
		};
	}

	protected void notifyValueChange()
	{
		for (ValueChangeListener listener : valueChangeListeners)
		{
			listener.valueChange(getValueChangeEvent());
		}
		this.validateField();

	}

	private boolean validateField()
	{
		boolean valid = false;
		try
		{
			setComponentError(null);
			validate();
			valid = true;
		}
		catch (final InvalidValueException e)
		{
			setComponentError(new ErrorMessage()
			{

				private static final long serialVersionUID = -2976235476811651668L;

				@Override
				public String getFormattedHtmlMessage()
				{
					return e.getHtmlMessage();
				}

				@Override
				public ErrorLevel getErrorLevel()
				{
					return ErrorLevel.ERROR;
				}
			});
		}
		return valid;

	}

	public void deselectAll()
	{
		if (selectedCheckBox != null)
		{
			selectedCheckBox.setValue(false);
		}

		selectedCheckBox = null;
		selectedId = null;

		notifyValueChange();

	}

	// public void addSelectionListener(SelectionListener listener)
	// {
	// markedIds.addSelectionListener(listener);
	//
	// }
}
