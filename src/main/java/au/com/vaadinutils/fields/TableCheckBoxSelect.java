package au.com.vaadinutils.fields;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.ErrorMessage.ErrorLevel;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class TableCheckBoxSelect extends Table
{

	private static final String TABLE_CHECK_BOX_SELECT = "TableCheckBoxSelect";
	private static final long serialVersionUID = -7559267854874304189L;
	Set<Object> markedIds = new TreeSet<Object>();
	boolean trackingSelected = true;
	private boolean multiselect;
	private boolean selectable = true;
	private Set<ValueChangeListener> valueChangeListeners = new HashSet<ValueChangeListener>();

	public TableCheckBoxSelect()
	{
		initCheckboxMultiSelect();
		setImmediate(true);
	}

	/**
	 * call this method after adding your custom fields
	 */
	public void initCheckboxMultiSelect()
	{

		this.addGeneratedColumn(TABLE_CHECK_BOX_SELECT, getGenerator());
		super.setMultiSelect(false);
		super.setSelectable(false);

	}

	public void setMultiSelect(boolean multi)
	{
		this.multiselect = multi;
	}

	public void selectAll()
	{
		markedIds.clear();
		trackingSelected = false;
		refreshRenderedCells();
		refreshRowCache();
		notifyValueChange();

	}

	public void deselectAll()
	{
		markedIds.clear();
		trackingSelected = true;
		refreshRenderedCells();
		refreshRowCache();
		notifyValueChange();

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
		if (selectable)
		{
			cols.add("");
		}
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
			if (selectable)
			{
				cols.add(TABLE_CHECK_BOX_SELECT);
			}
			super.setVisibleColumns(cols.toArray());
		}
		else
		{
			// during initialisation it comes through here empty and if we add
			// ours in npe's out
			super.setVisibleColumns(visibleColumns);
		}

	}

	@Override
	public boolean isMultiSelect()
	{
		return true;
	}

	/**
	 * use disableSelectable instead
	 */
	@Deprecated
	@Override
	public void setSelectable(boolean s)
	{
		throw new RuntimeException("Use disableSelectable instead");
	}

	public void disableSelectable()
	{
		selectable = false;
		removeGeneratedColumn(TABLE_CHECK_BOX_SELECT);
	}

	@Override
	public boolean isSelectable()
	{
		return true;
	}

	public Object getSelectedItems()
	{
		if (trackingSelected)
		{
			return markedIds;
		}
		TreeSet<Object> result = new TreeSet<Object>();
		result.addAll(getContainerDataSource().getItemIds());
		result.removeAll(markedIds);
		return result;
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

		TreeSet<Object> result = new TreeSet<Object>();

		return result;

	}

	private CheckBox lastChecked;

	private ColumnGenerator getGenerator()
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
				checkbox.addValueChangeListener(new ValueChangeListener()
				{

					private static final long serialVersionUID = 9170497247408214336L;

					@Override
					public void valueChange(Property.ValueChangeEvent event)
					{
						if (!multiselect)
						{
							markedIds.clear();
							if ((Boolean) event.getProperty().getValue() == trackingSelected)
							{
								markedIds.add(itemId);
								if (lastChecked != null)
								{
									lastChecked.setValue(false);
								}
								lastChecked = checkbox;
							}
							else
							{
								markedIds.remove(itemId);
								lastChecked = null;
							}

						}
						else
						{
							if ((Boolean) event.getProperty().getValue() == trackingSelected)
							{
								markedIds.add(itemId);
							}
							else
							{
								markedIds.remove(itemId);
							}
						}
						notifyValueChange();

					}

				});
				boolean inList = markedIds.contains(itemId);
				checkbox.setValue(inList);
				if (!trackingSelected)
				{
					checkbox.setValue(!inList);
				}
				checkbox.setImmediate(true);

				return checkbox;

			}
		};
	}

	private void notifyValueChange()
	{
		for (ValueChangeListener listener:valueChangeListeners)
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


}
