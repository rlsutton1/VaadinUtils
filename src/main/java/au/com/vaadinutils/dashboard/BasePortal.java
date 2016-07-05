package au.com.vaadinutils.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.alump.gridstack.GridStackMoveEvent;

import com.vaadin.data.sort.SortOrder;
import com.vaadin.event.SortEvent;
import com.vaadin.event.SortEvent.SortListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.ColumnReorderEvent;
import com.vaadin.ui.Grid.ColumnReorderListener;
import com.vaadin.ui.Grid.ColumnVisibilityChangeEvent;
import com.vaadin.ui.Grid.ColumnVisibilityChangeListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnCollapseEvent;
import com.vaadin.ui.Table.ColumnCollapseListener;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.ColumnResizeListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.JpaDslBuilder;

public abstract class BasePortal extends VerticalLayout implements Portal
{
	private static final String COLUMN_WIDTH = "column-width-";

	private static final long serialVersionUID = 1L;

	private static final String COLUMN_VISABLE = "column-visable-";

	String guid = null;

	Logger logger = LogManager.getLogger();
	private PortalConfigDelgate configDelegate;

	protected BasePortal(Tblportal portal, DashBoard dashBoard, PortalConfigDelgate configDelegate)
	{
		guid = portal.getGuid();

		this.configDelegate = configDelegate;
		setSizeFull();
		// setMargin(new MarginInfo(false, true, false, true));

		HorizontalLayout header = createHeader(configDelegate.getTitle(), dashBoard);

		addComponent(header);

	}

	public String getGuid()
	{
		return guid;
	}

	public Tblportal getPortal()
	{
		return JpaBaseDao.getGenericDao(Tblportal.class).findOneByAttribute(Tblportal_.guid, getGuid());

	}

	protected PortalConfigDelgate getConfigDelegate()
	{
		return configDelegate;
	}

	private HorizontalLayout createHeader(String title, final DashBoard dashBoard)
	{
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth("100%");
		header.setHeight("25");
		Label titleLabel = new Label(title);
		titleLabel.setStyleName(ValoTheme.LABEL_COLORED);
		header.addComponent(titleLabel);

		HorizontalLayout controlLayout = new HorizontalLayout();

		addCustomHeaderButtons(controlLayout);

		Button removeButton = new Button(FontAwesome.CLOSE);
		removeButton.setStyleName(ValoTheme.BUTTON_LINK);
		removeButton.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				dashBoard.removeComponent(BasePortal.this);
				Tblportal portal = JpaBaseDao.getGenericDao(Tblportal.class).findOneByAttribute(Tblportal_.guid, guid);
				if (portal != null)
				{
					portal.getPortalLayout().removePortal(portal);
					JpaDslBuilder<Tblportalconfig> q = JpaBaseDao.getGenericDao(Tblportalconfig.class).find();
					q.where(q.eq(Tblportalconfig_.portal, portal)).delete();
					EntityManagerProvider.remove(portal);
				}

			}
		});

		controlLayout.addComponent(removeButton);
		header.addComponent(controlLayout);
		header.setComponentAlignment(controlLayout, Alignment.MIDDLE_RIGHT);
		return header;
	}

	protected abstract void addCustomHeaderButtons(HorizontalLayout controlLayout);

	@Override
	public void savePosition(GridStackMoveEvent event)
	{
		Tblportal portal = JpaBaseDao.getGenericDao(Tblportal.class).findOneByAttribute(Tblportal_.guid, guid);
		configDelegate.savePosition(portal, event.getNew());
	}

	protected TableColumnManager configureSaveColumnWidths(final Table table, String tableId)
	{
		final Tblportal portal = getPortal();

		final String baseKey = tableId + "_" + COLUMN_WIDTH;

		for (Entry<String, Integer> value : getConfigDelegate().getValuesLikeInt(portal, baseKey).entrySet())
		{
			String key = value.getKey().substring(baseKey.length(), value.getKey().length());
			table.setColumnWidth(key, value.getValue());
		}

		table.addColumnResizeListener(new ColumnResizeListener()
		{
			private static final long serialVersionUID = 4034036880290943146L;

			@Override
			public void columnResize(ColumnResizeEvent event)
			{
				final Tblportal portal = getPortal();
				final String property = (String) event.getPropertyId();
				final int width = event.getCurrentWidth();
				getConfigDelegate().setValue(portal, baseKey + property, width);
			}
		});

		table.addColumnCollapseListener(new ColumnCollapseListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void columnCollapseStateChange(ColumnCollapseEvent event)
			{
				final Tblportal portal = getPortal();
				final String property = (String) event.getPropertyId();
				final boolean isVisible = !table.isColumnCollapsed(property);

			}
		});

		return new TableColumnManager()
		{

			@Override
			public void reset()
			{

				for (Entry<String, Integer> value : getConfigDelegate().getValuesLikeInt(getPortal(), baseKey)
						.entrySet())
				{
					String key = value.getKey().substring(baseKey.length(), value.getKey().length());
					table.setColumnExpandRatio(key, 1);
					getConfigDelegate().deleteValuesLike(getPortal(), baseKey);
				}

				for (Object col : table.getVisibleColumns())
				{
					table.setColumnExpandRatio(col, 1);
				}

			}
		};
	}

	protected TableColumnManager configureSaveColumnWidths(final Grid grid, String tableId)
	{
		final String baseWidthKey = tableId + "_" + COLUMN_WIDTH;
		final String baseVisableKey = tableId + "_" + COLUMN_VISABLE;
		final String keyStub = tableId + "_order";

		try
		{

			setupGridColumnSizing(grid, baseWidthKey);

			setupGridColumnVisibility(grid, baseVisableKey);

			setupGridColumnReordering(grid, keyStub);

			String keySorting = tableId + "_sort";
			setupGridSorting(grid, keySorting);

		}
		catch (Exception e)
		{
			logger.error(e, e);

		}
		return new TableColumnManager()
		{

			@Override
			public void reset()
			{

				for (Column col : grid.getColumns())
				{

					col.setWidthUndefined();
					getConfigDelegate().deleteValuesLike(getPortal(), baseWidthKey + col.getPropertyId());
				}

			}
		};
	}

	private void setupGridSorting(final Grid grid, final String keySorting)
	{

		String sorts = getConfigDelegate().getValueString(getPortal(), keySorting);
		if (StringUtils.isNotEmpty(sorts))
		{
			List<SortOrder> sortList = new LinkedList<>();
			String[] sortArray = sorts.split(",");
			for (String sort : sortArray)
			{
				sortList.add(new SortOrder(sort, SortDirection.ASCENDING));
			}
			grid.setSortOrder(sortList);

		}

		grid.addSortListener(new SortListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void sort(SortEvent event)
			{
				String sorts = "";
				for (SortOrder sort : event.getSortOrder())
				{
					sorts += sort.getPropertyId() + ",";
				}

				getConfigDelegate().setValue(getPortal(), keySorting, sorts);

			}
		});

	}

	private void setupGridColumnSizing(final Grid grid, final String baseWidthKey)
	{
		for (Entry<String, Integer> value : getConfigDelegate().getValuesLikeInt(getPortal(), baseWidthKey).entrySet())
		{
			try
			{
				String key = value.getKey().substring(baseWidthKey.length(), value.getKey().length());
				final Double width = value.getValue().doubleValue();
				if (width > 0)
				{

					grid.getColumn(key).setWidth(width);

				}
			}
			catch (Exception e)
			{
				logger.warn(e);
			}

		}

		grid.addColumnResizeListener(new Grid.ColumnResizeListener()
		{
			private static final long serialVersionUID = 4034036880290943146L;

			@Override
			public void columnResize(com.vaadin.ui.Grid.ColumnResizeEvent event)
			{
				final String propertyId = (String) event.getColumn().getPropertyId();
				final double width = event.getColumn().getWidth();
				getConfigDelegate().setValue(getPortal(), baseWidthKey + propertyId, "" + (int) width);

			}
		});
	}

	private void setupGridColumnVisibility(final Grid grid, final String baseVisableKey)
	{
		for (Entry<String, Integer> value : getConfigDelegate().getValuesLikeInt(getPortal(), baseVisableKey)
				.entrySet())
		{

			try
			{
				String key = value.getKey().substring(baseVisableKey.length(), value.getKey().length());
				Integer visable = value.getValue();

				grid.getColumn(key).setHidden(visable == 0);
			}
			catch (Exception e)
			{
				logger.warn(e);
			}

		}

		grid.addColumnVisibilityChangeListener(new ColumnVisibilityChangeListener()
		{

			private static final long serialVersionUID = -9082974567948595049L;

			@Override
			public void columnVisibilityChanged(ColumnVisibilityChangeEvent event)
			{
				final Column column = event.getColumn();
				final boolean isVisible = !column.isHidden();
				final Tblportal portal = getPortal();
				final String property = (String) column.getPropertyId();

				int value = 0;
				if (isVisible)
				{
					value = 1;
				}

				getConfigDelegate().setValue(getPortal(), baseVisableKey + column.getPropertyId(), value);

			}
		});
	}

	private void setupGridColumnReordering(final Grid grid, final String keyStub)
	{
		final List<Column> availableColumns = grid.getColumns();
		final String columns = getConfigDelegate().getValueString(getPortal(), keyStub);
		if (availableColumns.size() > 0 && columns != null && !columns.isEmpty())
		{
			final Object[] parsedColumns = columns.split(", ?");
			if (parsedColumns.length > 0)
			{
				grid.setColumns(calculateColumnOrder(availableColumns, parsedColumns));
			}
		}

		grid.addColumnReorderListener(new ColumnReorderListener()
		{
			private static final long serialVersionUID = -2810298692555333890L;

			@Override
			public void columnReorder(ColumnReorderEvent event)
			{
				final List<Column> columns = ((Grid) event.getSource()).getColumns();
				if (columns.size() > 0)
				{
					String parsedColumns = "";
					for (Column column : columns)
					{
						parsedColumns += column.getPropertyId() + ", ";
					}

					parsedColumns = parsedColumns.substring(0, parsedColumns.length() - 2);
					getConfigDelegate().setValue(getPortal(), keyStub, "" + parsedColumns);
				}
			}
		});
	}

	/**
	 * If a column order has already been saved for a user, but the columns for
	 * a grid have been modified, then we need to remove any columns that no
	 * longer exist and add any new columns to the list of visible columns.
	 *
	 * @param availableColumns
	 *            the columns that are available in the table
	 * @param parsedColumns
	 *            the column order that has been restored from preferences
	 * @return the calculated order of columns with old removed and new added
	 */
	private Object[] calculateColumnOrder(final List<Column> availableColumns, final Object[] parsedColumns)
	{
		final List<Object> availableList = new ArrayList<>(availableColumns.size());
		for (Column column : availableColumns)
		{
			availableList.add(column.getPropertyId());
		}
		final List<Object> parsedList = new ArrayList<>(Arrays.asList(parsedColumns));

		// Remove old columns
		parsedList.retainAll(availableList);

		// Add new columns in the same index position as they were added to the
		// table in
		final List<Object> newList = new ArrayList<>(availableList);
		newList.removeAll(parsedList);
		for (Object column : newList)
		{
			parsedList.add(availableList.indexOf(column), column);
		}

		return parsedList.toArray();
	}

}
