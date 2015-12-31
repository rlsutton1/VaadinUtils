package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class TableCheckBoxSelect extends Table
{

	public static final String TABLE_CHECK_BOX_SELECT = "TableCheckBoxSelect";
	private static final long serialVersionUID = -7559267854874304189L;
	protected MarkedIds markedIds = new MarkedIds();
	private boolean selectable = true;
	private Set<ValueChangeListener> valueChangeListeners = new HashSet<ValueChangeListener>();
	protected int containerSize = 0;

	Logger logger = LogManager.getLogger();

	public TableCheckBoxSelect()
	{
		initCheckboxMultiSelect();
		setImmediate(true);

		setId("TableCheckBoxSelect");

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
		if (!multi)
		{
			throw new RuntimeException(
					"This class is broken in single select mode, actually the single select code has been removed.\n\n Use TableCheckBoxSingleSelect instead!!!!\n\n");
		}

	}

	public void selectAll()
	{
		containerSize = getItemIds().size();
		markedIds.clear(false, containerSize);

		refreshRenderedCells();
		refreshRowCache();
		notifyValueChange();

	}

	public void deselectAll()
	{
		markedIds.clear(true, containerSize);

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
		super.setColumnHeaders(cols.toArray(new String[]
		{}));

	}

	@Override
	public void setVisibleColumns(Object... visibleColumns)
	{
		if (visibleColumns.length > 0)
		{
			List<Object> cols = new LinkedList<Object>();
			for (Object col : visibleColumns)
			{
				cols.add(col);
			}
			if (selectable)
			{
				cols.add(0, TABLE_CHECK_BOX_SELECT);
			}
			Set<Object> uniqueCols = new LinkedHashSet<>();
			uniqueCols.addAll(cols);
			super.setVisibleColumns(uniqueCols.toArray());
			setColumnWidth(TABLE_CHECK_BOX_SELECT, 50);
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
		final ArrayList<Object> v = new ArrayList<>(1);
		v.add(value);
		super.setValue(v);
	}

	@SuppressWarnings("unchecked")
	public void setSelectedValue(Object value)
	{
		// super.setValue(newValue);
		markedIds.clear(true, containerSize);
		markedIds.addAll((Collection<Long>) value);

		this.refreshRowCache();

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
		super.setSelectable(true);
		removeGeneratedColumn(TABLE_CHECK_BOX_SELECT);

	}

	@Override
	public boolean isSelectable()
	{
		return true;
	}

	public Object getSelectedItems()
	{
		if (selectable == false)
		{
			return super.getValue();
		}

		if (markedIds.isTrackingSelected())
		{
			return markedIds.getIds();
		}

		TreeSet<Object> result = new TreeSet<Object>();
		result.addAll(getContainerDataSource().getItemIds());
		result.removeAll(markedIds.getIds());
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
		return super.getValue();
	}

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

				// important that the following code is executed before the
				// value change listener is added
				boolean inList = markedIds.contains(itemId);
				checkbox.setValue(inList);
				checkbox.setId("checkboxSelect");
				if (!markedIds.isTrackingSelected())
				{
					checkbox.setValue(!inList);
				}

				checkbox.addValueChangeListener(new ValueChangeListener()
				{

					private static final long serialVersionUID = 9170497247408214336L;

					@Override
					public void valueChange(Property.ValueChangeEvent event)
					{

						if ((Boolean) event.getProperty().getValue() == markedIds.isTrackingSelected())
						{
							markedIds.add(itemId);
						}
						else
						{
							markedIds.remove(itemId);
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

	public void addSelectionListener(SelectionListener listener)
	{
		markedIds.addSelectionListener(listener);

	}
}
