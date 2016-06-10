package au.com.vaadinutils.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.ui.Grid;
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

public class GridHeadingPropertySet
{
	private List<GridHeadingToPropertyId> cols = new LinkedList<GridHeadingToPropertyId>();

	private Logger logger = LogManager.getLogger();

	private boolean eraseSavedConfig = false;

	private GridHeadingPropertySet()
	{
		// use the builder!
	}

	public static <E> Builder<E> getBuilder(Class<E> Class)
	{
		return new Builder<E>();
	}

	interface Start<E>
	{
		public AddingColumn<E> createColumn(String heading, String propertyId);

		public <T> AddingColumn<E> createColumn(String heading, SingularAttribute<E, T> headingPropertyId);

		public GridHeadingPropertySet build();
	}

	public interface AddingColumn<E>
	{

		public AddingColumn<E> setLockedState(boolean lockedState);

		public AddingColumn<E> setDefaultVisibleState(boolean defaultVisibleState);

		public AddingColumn<E> setWidth(Integer width);

		public AddingColumn<E> setColumnGenerator(PropertyValueGenerator<?> columnGenerator);

	}

	public static class Builder<E> implements AddingColumn<E>, Start<E>
	{
		private List<GridHeadingToPropertyId> cols = new LinkedList<GridHeadingToPropertyId>();

		private boolean eraseSavedConfig = false;

		@Override
		public GridHeadingPropertySet build()
		{

			addColumn();

			final GridHeadingPropertySet tmp = new GridHeadingPropertySet();
			tmp.cols = this.cols;
			tmp.eraseSavedConfig = eraseSavedConfig;

			return tmp;
		}

		public void setEraseSavedConfig()
		{
			eraseSavedConfig = true;
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

		private Builder<E> addColumn()
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

		/**
		 * Add a date column and format it.
		 *
		 * @param heading
		 *            - the headling label for this column
		 * @param column
		 *            - the Date column that is to be displayed in the column
		 * @param format
		 *            - the format for the Date. format is passed to a
		 *            SimpleDateFormat
		 */
		// public Builder<E> addColumn(String headingLabel, SingularAttribute<E,
		// Date> column, String dateFormat,
		// int width)
		// {
		// return addGeneratedColumn(headingLabel, column.getName(),
		// new DateColumnGenerator<E>(column.getName(), dateFormat), true,
		// false, width);
		// }

		/**
		 * Add a date column and format it.
		 *
		 * @param heading
		 *            - the headling label for this column
		 * @param column
		 *            - the Date column that is to be displayed in the column
		 * @param format
		 *            - the format for the Date. format is passed to a
		 *            SimpleDateFormat
		 */
		// public Builder<E> addColumn(String headingLabel, String
		// headingPropertyId, String dateFormat, int width)
		// {
		// // We make the alias the same as the underlying property so that we
		// // can sort this column.
		// // Generated columns are not normally sortable however by mapping
		// // our generated column to the underlying date column our generated
		// // column becomes sortable.
		// return addGeneratedColumn(headingLabel, headingPropertyId,
		// new DateColumnGenerator<E>(headingPropertyId, dateFormat), true,
		// false, width);
		//
		// }

	}

	/**
	 * Date Column generator used to format Date columns.
	 *
	 * @author bsutton
	 *
	 * @param <E>
	 */
	// static class DateColumnGenerator<E> implements ColumnGenerator
	// {
	// private static final long serialVersionUID = 1;
	// private Logger logger = LogManager.getLogger();
	//
	// final private SimpleDateFormat sdf;
	// final private SimpleDateFormat sdfParse = new
	// SimpleDateFormat("yyyy-MM-dd");
	// final private String headingPropertyId;
	//
	// DateColumnGenerator(String headingPropertyId, String format)
	// {
	// this.headingPropertyId = headingPropertyId;
	// this.sdf = new SimpleDateFormat(format);
	// }
	//
	// @Override
	// public Object generateCell(Table source, Object itemId, Object columnId)
	// {
	// Item item = source.getItem(itemId);
	//
	// Object objDate = item.getItemProperty(headingPropertyId).getValue();
	//
	// String formattedDate = "";
	//
	// if (objDate instanceof Date)
	// {
	// formattedDate = sdf.format((Date) objDate);
	// }
	// else if (objDate != null)
	// {
	// String strDate = objDate.toString();
	// try
	// {
	// formattedDate = sdf.format(sdfParse.parse(strDate));
	// }
	// catch (ParseException e)
	// {
	// // just so we have a value.
	// formattedDate = "Invalid";
	// logger.error(
	// "Looks like our assumptions about the format of dates is wrong. Please
	// update the parse format to match:"
	// + strDate +" "+sdf.toPattern());
	// }
	// }
	//
	// Label label = new Label(formattedDate);
	//
	// return label;
	// }
	//
	// }

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
		try
		{
			final GeneratedPropertyContainer gpc = wrapGridContainer(grid);

			final List<String> colsToShow = new LinkedList<String>();
			for (GridHeadingToPropertyId column : getColumns())
			{
				final String propertyId = column.getPropertyId();
				if (column.isGenerated())
				{
					final PropertyValueGenerator<?> columnGenerator = column.getColumnGenerator();
					gpc.addGeneratedProperty(propertyId, columnGenerator);
					final Column gridColumn = grid.getColumn(propertyId);
					if (columnGenerator.getType() == String.class && gridColumn.getRenderer() instanceof TextRenderer)
					{
						gridColumn.setRenderer(new HtmlRenderer(), null);
					}
				}
				else
				{
					Preconditions.checkArgument(
							grid.getContainerDataSource().getContainerPropertyIds().contains(propertyId),
							propertyId + " is not a valid property id, valid property ids are "
									+ grid.getContainerDataSource().getContainerPropertyIds().toString());
				}

				grid.getDefaultHeaderRow().getCell(propertyId).setText(column.getHeader());
				colsToShow.add(propertyId);
				final Column gridColumn = grid.getColumn(propertyId);

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

			configureSaveColumnWidths(grid, uniqueId);
			configureSaveColumnOrder(grid, uniqueId);
			configureSaveColumnVisible(grid, uniqueId);
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
			final GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(grid.getContainerDataSource());
			grid.setContainerDataSource(gpc);
			gridContainer = gpc;
		}

		return (GeneratedPropertyContainer) gridContainer;
	}

	private void configureSaveColumnWidths(final Grid grid, final String uniqueId)
	{
		final String keyStub = uniqueId + "-width";

		for (GridHeadingToPropertyId id : getColumns())
		{
			final String setWidth = UserSettingsStorageFactory.getUserSettingsStorage()
					.get(keyStub + "-" + id.getPropertyId());
			if (setWidth != null && setWidth.length() > 0)
			{
				grid.getColumn(id.getPropertyId()).setWidth(Double.parseDouble(setWidth));
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
}
