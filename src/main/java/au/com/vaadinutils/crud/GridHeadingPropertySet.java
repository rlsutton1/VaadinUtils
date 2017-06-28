package au.com.vaadinutils.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.Logger;

import com.ejt.vaadin.sizereporter.ComponentResizeEvent;
import com.ejt.vaadin.sizereporter.ComponentResizeListener;
import com.ejt.vaadin.sizereporter.SizeReporter;
import com.google.common.base.Preconditions;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.AbstractRenderer;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.ColumnReorderEvent;
import com.vaadin.ui.Grid.ColumnReorderListener;
import com.vaadin.ui.Grid.ColumnResizeEvent;
import com.vaadin.ui.Grid.ColumnResizeListener;
import com.vaadin.ui.Grid.ColumnVisibilityChangeEvent;
import com.vaadin.ui.Grid.ColumnVisibilityChangeListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.TextRenderer;

import au.com.vaadinutils.dao.Path;
import au.com.vaadinutils.user.UserSettingsStorageFactory;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;

public class GridHeadingPropertySet<E> implements GridHeadingPropertySetIfc<E>
{
	private Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private List<GridHeadingToPropertyId> cols = new LinkedList<>();
	private boolean eraseSavedConfig = false;
	private Grid grid;
	private String uniqueId;
	private boolean dynamicColumnWidth = false;

	// Set to true if you would like to defer loading settings until
	// applySettingsToColumns is called
	private boolean deferLoadSettings = false;

	public GridHeadingPropertySet(final List<GridHeadingToPropertyId> cols)
	{
		this.cols = cols;
	}

	public static <E> Builder<E> getBuilder(Class<E> Class)
	{
		return new Builder<>();
	}

	interface Start<E>
	{
		public AddingColumn<E> createColumn(String heading, String propertyId);

		public <T> AddingColumn<E> createColumn(String heading, SingularAttribute<E, T> headingPropertyId);

		public GridHeadingPropertySet<E> build();
	}

	public interface AddingColumn<E>
	{

		public AddingColumn<E> setLockedState(boolean lockedState);

		public AddingColumn<E> setDefaultVisibleState(boolean defaultVisibleState);

		public AddingColumn<E> setWidth(Integer width);

		public AddingColumn<E> setColumnGenerator(PropertyValueGenerator<?> columnGenerator);

		public AddingColumn<E> setRenderer(AbstractRenderer<?> renderer);

		public GridHeadingPropertySet<E> build();

		public AddingColumn<E> setConverter(Converter<String, ?> converter);

		public Builder<E> addColumn();

	}

	public static class Builder<E> implements AddingColumn<E>, Start<E>
	{
		private List<GridHeadingToPropertyId> cols = new LinkedList<>();

		private boolean eraseSavedConfig = false;
		private boolean dynamicColumnWidth = false;

		@Override
		public GridHeadingPropertySet<E> build()
		{
			addColumn();
			final GridHeadingPropertySet<E> propertySet = new GridHeadingPropertySet<>(this.cols);
			propertySet.eraseSavedConfig = eraseSavedConfig;
			propertySet.dynamicColumnWidth = dynamicColumnWidth;

			return propertySet;
		}

		@Override
		public AddingColumn<E> setRenderer(AbstractRenderer<?> renderer)
		{
			columnBuilder.setRenderer(renderer);
			return this;
		}

		@Override
		public AddingColumn<E> setConverter(Converter<String, ?> converter)
		{
			columnBuilder.setConverter(converter);
			return this;
		}

		public void setEraseSavedConfig()
		{
			eraseSavedConfig = true;
		}

		/**
		 * Setting dynamic column width ensures that the total width of columns
		 * is always equal to the width of the component. This prevents
		 * horizontal scrolling as well as ensures that the complete width of
		 * the component is always utilised. If a column is resized, then all
		 * columns to the right of it are also resized based on how much space
		 * there is remaining to the edge of the component.
		 */
		public void setDynamicColumnWidth()
		{
			dynamicColumnWidth = true;
		}

		/* Add column methods */

		/**
		 * Add a new table column
		 *
		 * @param heading
		 *            the column heading that will be displayed
		 * @param headingPropertyId
		 *            the heading property id
		 * @param defaultVisibleState
		 *            whether the column is visible by default
		 * @param lockedState
		 *            whether the visibility of a column can be modified
		 * @param width
		 *            the width of the column
		 * @return the builder
		 */
		public Builder<E> addColumn(final String heading, final String headingPropertyId,
				final boolean defaultVisibleState, final boolean lockedState, final int width)
		{
			createColumn(heading, headingPropertyId).setDefaultVisibleState(defaultVisibleState)
					.setLockedState(lockedState).setWidth(width);

			this.addColumn();
			return this;
		}

		GridHeadingToPropertyId.Builder columnBuilder = null;

		@Override
		public AddingColumn<E> createColumn(String heading, String propertyId)
		{

			addColumn();

			columnBuilder = new GridHeadingToPropertyId.Builder(heading, propertyId);
			return this;
		}

		@Override
		public <T> AddingColumn<E> createColumn(String heading, SingularAttribute<E, T> headingPropertyId)
		{

			addColumn();

			columnBuilder = new GridHeadingToPropertyId.Builder(heading, headingPropertyId.getName());
			return this;

		}

		@Override
		public Builder<E> addColumn()
		{
			if (columnBuilder != null)
			{
				cols.add(columnBuilder.build());
			}
			// fail fast rather than have weird behaviour
			columnBuilder = null;
			return this;
		}

		@Override
		public AddingColumn<E> setLockedState(boolean lockedState)
		{
			columnBuilder.setLockedState(lockedState);
			return this;
		}

		@Override
		public AddingColumn<E> setDefaultVisibleState(boolean defaultVisibleState)
		{
			columnBuilder.setDefaultVisibleState(defaultVisibleState);
			return this;
		}

		@Override
		public AddingColumn<E> setWidth(Integer width)
		{
			columnBuilder.setWidth(width);
			return this;
		}

		@Override
		public AddingColumn<E> setColumnGenerator(PropertyValueGenerator<?> columnGenerator)
		{
			columnBuilder.setColumnGenerator(columnGenerator);
			return this;
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final boolean defaultVisibleState,
				final boolean lockedState, int width)
		{
			createColumn(heading, headingPropertyId.getName()).setDefaultVisibleState(defaultVisibleState)
					.setLockedState(lockedState).setWidth(width);
			this.addColumn();
			return this;
		}

		public <T extends Object> Builder<E> addColumn(final String heading, final String headingPropertyId,
				final boolean defaultVisibleState, final boolean lockedState)
		{
			cols.add(new GridHeadingToPropertyId(heading, headingPropertyId, null, defaultVisibleState, lockedState,
					null));
			return this;
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final boolean defaultVisibleState,
				final boolean lockedState)
		{
			return addColumn(heading, headingPropertyId.getName(), defaultVisibleState, lockedState);
		}

		/* Add generated column methods */

		/**
		 * Add a new generated table column
		 *
		 * @param heading
		 *            the column heading that will be displayed
		 * @param headingPropertyId
		 *            the heading property id
		 * @param columnGenerator
		 *            the column generator
		 * @param defaultVisibleState
		 *            whether the column is visible by default
		 * @param lockedState
		 *            whether the visibility of a column can be modified
		 * @param width
		 *            the width of the column
		 * @return the builder
		 */
		public Builder<E> addGeneratedColumn(final String heading, final String headingPropertyId,
				final PropertyValueGenerator<?> columnGenerator, final boolean defaultVisibleState,
				final boolean lockedState, final int width)
		{
			cols.add(new GridHeadingToPropertyId(heading, headingPropertyId, columnGenerator, defaultVisibleState,
					lockedState, width));
			return this;
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final PropertyValueGenerator<?> columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState, int width)
		{
			return addGeneratedColumn(heading, headingPropertyId.getName(), columnGenerator, defaultVisibleState,
					lockedState, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final String headingPropertyId,
				final PropertyValueGenerator<?> columnGenerator, final boolean defaultVisibleState,
				final boolean lockedState)
		{
			cols.add(new GridHeadingToPropertyId(heading, headingPropertyId, columnGenerator, defaultVisibleState,
					lockedState, null));
			return this;
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final PropertyValueGenerator<?> columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState)
		{
			return addGeneratedColumn(heading, headingPropertyId.getName(), columnGenerator, defaultVisibleState,
					lockedState);
		}

		public Builder<E> addGeneratedColumn(final String heading, final PropertyValueGenerator<?> columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState, int width)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator, defaultVisibleState,
					lockedState, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final PropertyValueGenerator<?> columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator, defaultVisibleState,
					lockedState);
		}

		/* Add column convenience methods */

		public <T extends Object> Builder<E> addColumn(final String heading, final String headingPropertyId,
				final int width)
		{
			return addColumn(heading, headingPropertyId, true, false, width);
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final int width)
		{
			return addColumn(heading, headingPropertyId, true, false, width);
		}

		public Builder<E> addColumn(final String heading, final String headingPropertyId)
		{
			return addColumn(heading, headingPropertyId, true, false);
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId)
		{
			return addColumn(heading, headingPropertyId, true, false);
		}

		public <T extends Object> Builder<E> addColumn(String heading, Path pathToHeadingPropertyId)
		{
			return addColumn(heading, pathToHeadingPropertyId.getName());
		}

		/* Add hidden column convenience methods */

		public Builder<E> addHiddenColumn(final String heading, final String headingPropertyId, final int width)
		{
			return addColumn(heading, headingPropertyId, false, false, width);
		}

		public <T extends Object> Builder<E> addHiddenColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final int width)
		{
			return addColumn(heading, headingPropertyId, false, false, width);
		}

		public Builder<E> addHiddenColumn(final String heading, final String headingPropertyId)
		{
			return addColumn(heading, headingPropertyId, false, false);
		}

		public <T extends Object> Builder<E> addHiddenColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId)
		{
			return addColumn(heading, headingPropertyId, false, false);
		}

		/* Add generated column convenience methods */

		public Builder<E> addGeneratedColumn(final String heading, final String headingPropertyId,
				final PropertyValueGenerator<?> columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false, width);
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final PropertyValueGenerator<?> columnGenerator,
				final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final String headingPropertyId,
				final PropertyValueGenerator<?> columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false);
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final PropertyValueGenerator<?> columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false);
		}

		public Builder<E> addGeneratedColumn(final String heading, final PropertyValueGenerator<?> columnGenerator,
				final int width)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final PropertyValueGenerator<?> columnGenerator)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator);
		}

		/* Add hidden generated column convenience methods */

		public Builder<E> addHiddenGeneratedColumn(final String heading, final String headingPropertyId,
				final PropertyValueGenerator<?> columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false, width);
		}

		public <T extends Object> Builder<E> addHiddenGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final PropertyValueGenerator<?> columnGenerator,
				final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false, width);
		}

		public Builder<E> addHiddenGeneratedColumn(final String heading, final String headingPropertyId,
				final PropertyValueGenerator<?> columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false);
		}

		public <T extends Object> Builder<E> addHiddenGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final PropertyValueGenerator<?> columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false);
		}

		public Builder<E> addHiddenGeneratedColumn(final String heading,
				final PropertyValueGenerator<?> columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, columnGenerator, false, false, width);
		}

		public Builder<E> addHiddenGeneratedColumn(final String heading,
				final PropertyValueGenerator<?> columnGenerator)
		{
			return addGeneratedColumn(heading, columnGenerator, false, false);
		}
	}

	public List<GridHeadingToPropertyId> getColumns()
	{
		return cols;
	}

	public void applyToGrid(Grid grid)
	{
		final StackTraceElement[] trace = new Exception().getStackTrace();
		for (StackTraceElement call : trace)
		{
			if (!call.getClassName().contains("au.com.vaadinutils"))
			{
				if (call.getClassName().contains("."))
				{
					applyToGrid(grid, call.getClassName().substring(call.getClassName().lastIndexOf(".")));
				}
				else
				{
					applyToGrid(grid, call.getClassName());
				}

				return;
			}
		}

		throw new RuntimeException("Unable to determine calling class name, "
				+ " use applyToTable(Table table, String uniqueTableId) " + " instead of applyToTable(Table table)");
	}

	/**
	 *
	 * @param grid
	 * @param uniqueId
	 *            - an id for this layout/grid combination, it is used to
	 *            identify stored column widths in a key value map
	 */

	public void applyToGrid(final Grid grid, final String uniqueId)
	{
		this.grid = grid;
		this.uniqueId = uniqueId;

		try
		{
			// Changing the bound container at this point can cause issues
			// elsewhere, so we avoid it if possible
			Indexed gridContainer = grid.getContainerDataSource();
			for (GridHeadingToPropertyId column : getColumns())
			{
				if (column.isGenerated())
				{
					gridContainer = wrapGridContainer(grid);
					break;
				}
			}

			final List<String> colsToShow = new LinkedList<>();
			for (GridHeadingToPropertyId column : getColumns())
			{
				final String propertyId = column.getPropertyId();
				if (column.isGenerated())
				{
					final PropertyValueGenerator<?> columnGenerator = column.getColumnGenerator();
					((GeneratedPropertyContainer) gridContainer).addGeneratedProperty(propertyId, columnGenerator);
					final Column gridColumn = grid.getColumn(propertyId);
					if (columnGenerator.getType() == String.class && gridColumn.getRenderer() instanceof TextRenderer)
					{
						gridColumn.setRenderer(new HtmlRenderer(), null);
					}
					else if (columnGenerator.getType() == Component.class)
					{
						gridColumn.setRenderer(new ComponentRenderer());
					}
				}
				else
				{
					Preconditions.checkArgument(
							grid.getContainerDataSource().getContainerPropertyIds().contains(propertyId),
							propertyId + " is not a valid property id, valid property ids are "
									+ grid.getContainerDataSource().getContainerPropertyIds().toString());
				}

				colsToShow.add(propertyId);
				final Column gridColumn = grid.getColumn(propertyId);

				if (column.getRenderer() != null)
				{
					gridColumn.setRenderer(column.getRenderer());
				}

				if (column.getConverter() != null)
				{
					gridColumn.setConverter(column.getConverter());
				}

				gridColumn.setHeaderCaption(column.getHeader());

				if (column.getWidth() != null)
				{
					gridColumn.setWidth(column.getWidth());
				}
				else
				{
					gridColumn.setExpandRatio(1);
					gridColumn.setMinimumWidth(1);
				}

				if (column.isLocked())
				{
					gridColumn.setHidable(false);
				}
				else
				{
					gridColumn.setHidable(true);
					if (!column.isVisibleByDefault())
					{
						gridColumn.setHidden(true);
					}
				}
			}

			grid.setColumns(colsToShow.toArray());

			if (eraseSavedConfig)
			{
				eraseSavedConfig(uniqueId);
			}

			if (!deferLoadSettings)
			{
				configureSaveColumnWidths(grid, uniqueId);
				configureSaveColumnOrder(grid, uniqueId);
				configureSaveColumnVisible(grid, uniqueId);
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	void eraseSavedConfig(final String uniqueTableId)
	{
		UserSettingsStorageFactory.getUserSettingsStorage().erase(uniqueTableId);
	}

	private GeneratedPropertyContainer wrapGridContainer(final Grid grid)
	{
		Indexed gridContainer = grid.getContainerDataSource();
		if (!(gridContainer instanceof GeneratedPropertyContainer))
		{
			final GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(gridContainer);
			grid.setContainerDataSource(gpc);
			gridContainer = gpc;
		}

		return (GeneratedPropertyContainer) gridContainer;
	}

	@Override
	public void setDeferLoadSettings(final boolean deferLoadSettings)
	{
		this.deferLoadSettings = deferLoadSettings;
	}

	@Override
	public void applySettingsToColumns()
	{
		Preconditions.checkState(grid != null, "You must call applytoGrid first");

		configureSaveColumnWidths(grid, uniqueId);
		configureSaveColumnOrder(grid, uniqueId);
		configureSaveColumnVisible(grid, uniqueId);
	}

	private void configureSaveColumnWidths(final Grid grid, final String uniqueId)
	{
		final String keyStub = uniqueId + "-width";

		for (GridHeadingToPropertyId column : getColumns())
		{
			final String columnId = column.getPropertyId();
			final String setting = keyStub + "-" + columnId;
			final String columnWidth = UserSettingsStorageFactory.getUserSettingsStorage().get(setting);
			if (columnWidth != null && columnWidth.length() > 0)
			{
				try
				{
					final Double width = Double.parseDouble(columnWidth);
					if (width > 0)
					{
						grid.getColumn(columnId).setWidth(Double.parseDouble(columnWidth));
					}
				}
				catch (NumberFormatException e)
				{
					logger.error("Invalid width setting for " + setting);
				}
			}
		}

		grid.addColumnResizeListener(new ColumnResizeListener()
		{
			private static final long serialVersionUID = 4034036880290943146L;

			@Override
			public void columnResize(ColumnResizeEvent event)
			{
				final String propertyId = (String) event.getColumn().getPropertyId();
				final double width = event.getColumn().getWidth();
				UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + propertyId, "" + width);
			}
		});

		if (dynamicColumnWidth)
		{
			configureDynamicColumnWidth();
		}
	}

	private void configureDynamicColumnWidth()
	{

		final AtomicBoolean resizing = new AtomicBoolean(false);
		final AtomicInteger gridWidth = new AtomicInteger();
		final SizeReporter sizeReporter = new SizeReporter(grid);
		sizeReporter.addResizeListener(new ComponentResizeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void sizeChanged(ComponentResizeEvent event)
			{
				final int newGridWidth = event.getWidth();
				gridWidth.set(newGridWidth);

				if (newGridWidth > 1)
				{
					final List<Column> gridColumns = grid.getColumns();

					double columnsTotalWidth = 0;
					for (Column column : gridColumns)
					{
						columnsTotalWidth += column.getWidth();
					}

					final double widthDiscrepancy = gridWidth.get() - columnsTotalWidth;

					if (widthDiscrepancy != 0)
					{
						final double perColumnChange = widthDiscrepancy / gridColumns.size();

						for (Column column : gridColumns)
						{
							resizing.set(true);
							column.setWidth(column.getWidth() + perColumnChange);
							resizing.set(false);
						}
					}
				}
			}
		});

		grid.addColumnResizeListener(new ColumnResizeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void columnResize(ColumnResizeEvent event)
			{
				if (gridWidth.get() > 0 && !resizing.get())
				{
					resizing.set(true);

					final List<Column> gridColumns = grid.getColumns();
					final int totalColumns = gridColumns.size();
					final Column resizedColumn = event.getColumn();
					final double resizedColumnWidth = resizedColumn.getWidth();
					final int resizedColumnIndex = gridColumns.indexOf(resizedColumn);

					// availableWidth = grid width - width of column being
					// resized - widths of columns to the left
					double availableWidth = gridWidth.get() - resizedColumnWidth;
					for (int i = 0; i < resizedColumnIndex; i++)
					{
						availableWidth -= gridColumns.get(i).getWidth();
					}

					// columnsToResize = total columns - column being resized -
					// number of columns to the right
					final int columnsToResize = totalColumns - resizedColumnIndex - 1;
					final double perColumnWidth = availableWidth / columnsToResize;

					for (int i = (resizedColumnIndex + 1); i < totalColumns; i++)
					{
						gridColumns.get(i).setWidth(perColumnWidth);
					}

					resizing.set(false);
				}
			}
		});
	}

	private void configureSaveColumnOrder(final Grid grid, final String uniqueId)
	{
		final String keyStub = uniqueId + "-order";

		final List<Column> availableColumns = grid.getColumns();
		final String columns = UserSettingsStorageFactory.getUserSettingsStorage().get(keyStub);
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
					UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub, "" + parsedColumns);
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

	private void configureSaveColumnVisible(final Grid grid, final String uniqueId)
	{
		final String keyStub = uniqueId + "-visible";

		for (GridHeadingToPropertyId id : getColumns())
		{
			final String setVisible = UserSettingsStorageFactory.getUserSettingsStorage()
					.get(keyStub + "-" + id.getPropertyId());
			if (setVisible != null && !setVisible.isEmpty())
			{
				grid.getColumn(id.getPropertyId()).setHidden(!Boolean.parseBoolean(setVisible));
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
				UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + column.getPropertyId(),
						"" + isVisible);
			}
		});
	}

	@Override
	public String toString()
	{
		return Arrays.toString(cols.toArray());
	}

	@Override
	public void applyToGrid(Class<E> entityClazz, Grid grid, String uniqueId)
	{
		applyToGrid(grid, uniqueId);
	}
}
