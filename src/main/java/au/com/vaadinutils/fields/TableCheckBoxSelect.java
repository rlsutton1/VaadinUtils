package au.com.vaadinutils.fields;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Reindeer;

public class TableCheckBoxSelect extends Table
{

	private static final long serialVersionUID = -7559267854874304189L;
	Set<Object> markedIds = new TreeSet<Object>();
	boolean trackingSelected = true;
	private boolean multiselect;

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

		this.addGeneratedColumn("TableCheckBoxSelect", getGenerator());
		super.setMultiSelect(false);
		setSelectable(false);

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
	}

	public void deselectAll()
	{
		markedIds.clear();
		trackingSelected = true;
		refreshRenderedCells();
		refreshRowCache();

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
			cols.add("TableCheckBoxSelect");
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
				checkbox.setWidth("20");
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
}
