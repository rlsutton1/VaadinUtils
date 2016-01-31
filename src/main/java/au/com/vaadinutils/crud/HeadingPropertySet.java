package au.com.vaadinutils.crud;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnCollapseEvent;
import com.vaadin.ui.Table.ColumnCollapseListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnReorderEvent;
import com.vaadin.ui.Table.ColumnReorderListener;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.ColumnResizeListener;

import au.com.vaadinutils.dao.Path;
import au.com.vaadinutils.user.UserSettingsStorageFactory;

public class HeadingPropertySet<E>
{
	private List<HeadingToPropertyId<E>> cols = new LinkedList<HeadingToPropertyId<E>>();

	private Logger logger = LogManager.getLogger();

	private HeadingPropertySet()
	{
		// use the builder!
	}

	public static <E> Builder<E> getBuilder(Class<E> Class)
	{
		return new Builder<E>();
	}

	public static class Builder<E>
	{
		private List<HeadingToPropertyId<E>> cols = new LinkedList<HeadingToPropertyId<E>>();

		public HeadingPropertySet<E> build()
		{
			final HeadingPropertySet<E> tmp = new HeadingPropertySet<E>();
			tmp.cols = this.cols;

			return tmp;
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
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null, defaultVisibleState, lockedState,
					null));
			return this;
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final boolean defaultVisibleState,
				final boolean lockedState, int width)
		{
			return addColumn(heading, headingPropertyId.getName(), defaultVisibleState, lockedState, width);
		}

		public <T extends Object> Builder<E> addColumn(final String heading, final String headingPropertyId,
				final boolean defaultVisibleState, final boolean lockedState)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null, defaultVisibleState, lockedState,
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
				final ColumnGenerator columnGenerator, final boolean defaultVisibleState, final boolean lockedState,
				final int width)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, columnGenerator, defaultVisibleState,
					lockedState, width));
			return this;
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final ColumnGenerator columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState, int width)
		{
			return addGeneratedColumn(heading, headingPropertyId.getName(), columnGenerator, defaultVisibleState,
					lockedState, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final String headingPropertyId,
				final ColumnGenerator columnGenerator, final boolean defaultVisibleState, final boolean lockedState)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, columnGenerator, defaultVisibleState,
					lockedState, null));
			return this;
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final ColumnGenerator columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState)
		{
			return addGeneratedColumn(heading, headingPropertyId.getName(), columnGenerator, defaultVisibleState,
					lockedState);
		}

		public Builder<E> addGeneratedColumn(final String heading, final ColumnGenerator columnGenerator,
				final boolean defaultVisibleState, final boolean lockedState, int width)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator, defaultVisibleState,
					lockedState, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final ColumnGenerator columnGenerator,
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
				final ColumnGenerator columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false, width);
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final ColumnGenerator columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final String headingPropertyId,
				final ColumnGenerator columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false);
		}

		public <T extends Object> Builder<E> addGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final ColumnGenerator columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, true, false);
		}

		public Builder<E> addGeneratedColumn(final String heading, final ColumnGenerator columnGenerator,
				final int width)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator, width);
		}

		public Builder<E> addGeneratedColumn(final String heading, final ColumnGenerator columnGenerator)
		{
			return addGeneratedColumn(heading, heading + "-generated", columnGenerator);
		}

		/* Add hidden generated column convenience methods */

		public Builder<E> addHiddenGeneratedColumn(final String heading, final String headingPropertyId,
				final ColumnGenerator columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false, width);
		}

		public <T extends Object> Builder<E> addHiddenGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final ColumnGenerator columnGenerator, final int width)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false, width);
		}

		public Builder<E> addHiddenGeneratedColumn(final String heading, final String headingPropertyId,
				final ColumnGenerator columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false);
		}

		public <T extends Object> Builder<E> addHiddenGeneratedColumn(final String heading,
				final SingularAttribute<E, T> headingPropertyId, final ColumnGenerator columnGenerator)
		{
			return addGeneratedColumn(heading, headingPropertyId, columnGenerator, false, false);
		}

		public Builder<E> addHiddenGeneratedColumn(final String heading, final ColumnGenerator columnGenerator,
				final int width)
		{
			return addGeneratedColumn(heading, columnGenerator, false, false, width);
		}

		public Builder<E> addHiddenGeneratedColumn(final String heading, final ColumnGenerator columnGenerator)
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
		public Builder<E> addColumn(String headingLabel, SingularAttribute<E, Date> column, String dateFormat,
				int width)
		{
			return addGeneratedColumn(headingLabel, column.getName(),
					new DateColumnGenerator<E>(column.getName(), dateFormat), true, false, width);
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
		public Builder<E> addColumn(String headingLabel, String headingPropertyId, String dateFormat, int width)
		{
			// We make the alias the same as the underlying property so that we
			// can sort this column.
			// Generated columns are not normally sortable however by mapping
			// our generated column to the underlying date column our generated
			// column becomes sortable.
			return addGeneratedColumn(headingLabel, headingPropertyId,
					new DateColumnGenerator<E>(headingPropertyId, dateFormat), true, false, width);

		}

	}

	/**
	 * Date Column generator used to format Date columns.
	 *
	 * @author bsutton
	 *
	 * @param <E>
	 */

	static class DateColumnGenerator<E> implements ColumnGenerator
	{
		private static final long serialVersionUID = 1;
		private Logger logger = LogManager.getLogger();

		final private SimpleDateFormat sdf;
		final private SimpleDateFormat sdfParse = new SimpleDateFormat("YYYY-MM-DD");
		final private String headingPropertyId;

		DateColumnGenerator(String headingPropertyId, String format)
		{
			this.headingPropertyId = headingPropertyId;
			this.sdf = new SimpleDateFormat(format);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId)
		{
			@SuppressWarnings("unchecked")
			EntityItem<E> item = (EntityItem<E>) source.getItem(itemId);

			EntityItemProperty dateProperty = item.getItemProperty(headingPropertyId);
			Object objDate = dateProperty.getValue();
			String strDate = objDate.toString();
			Date date;
			if (objDate instanceof Date)
			{
				date = (Date) objDate;
			}
			else
			{
				strDate = objDate.toString();
				try
				{
					date = sdfParse.parse(strDate);

				}
				catch (ParseException e)
				{
					// just so we have a value.
					date = new Date();
					logger.error(
							"Looks like our assumptions about the format of dates stored in EntityItems is wrong. Please update the parse format to match:"
									+ strDate);
				}

			}
			String formattedDate = sdf.format(date);
			Label label = new Label(formattedDate);

			return label;
		}

	};

	public List<HeadingToPropertyId<E>> getColumns()
	{
		return cols;
	}

	public void applyToTable(Table table)
	{
		final StackTraceElement[] trace = new Exception().getStackTrace();
		for (StackTraceElement call : trace)
		{
			if (!call.getClassName().contains("au.com.vaadinutils"))
			{
				if (call.getClassName().contains("."))
				{
					applyToTable(table, call.getClassName().substring(call.getClassName().lastIndexOf(".")));
				}
				else
				{
					applyToTable(table, call.getClassName());
				}

				return;
			}
		}

		throw new RuntimeException("Unable to determine calling class name, "
				+ " use applyToTable(Table table, String uniqueTableId) " + " instead of applyToTable(Table table)");
	}

	/**
	 *
	 * @param table
	 * @param uniqueTableId
	 *            - an id for this layout/table combination, it is used to
	 *            identify stored column widths in a key value map
	 */
	public void applyToTable(final Table table, final String uniqueTableId)
	{
		try
		{
			final List<String> colsToShow = new LinkedList<String>();
			for (HeadingToPropertyId<E> column : getColumns())
			{
				colsToShow.add(column.getPropertyId());
				table.setColumnHeader(column.getPropertyId(), column.getHeader());

				if (column.isGenerated())
				{
					table.addGeneratedColumn(column.getPropertyId(), column.getColumnGenerator());
				}
				else
				{
					Preconditions.checkArgument(table.getContainerPropertyIds().contains(column.getPropertyId()),
							column.getPropertyId() + " is not a valid property id, valid property ids are "
									+ table.getContainerPropertyIds().toString());
				}

				if (column.getWidth() != null)
				{
					table.setColumnWidth(column.getPropertyId(), column.getWidth());
				}

				if (!column.isVisibleByDefault())
				{
					table.setColumnCollapsingAllowed(true);
					table.setColumnCollapsed(column.getPropertyId(), true);
				}

				if (column.isLocked())
					table.setColumnCollapsible(column.getPropertyId(), false);

			}
			table.setVisibleColumns(colsToShow.toArray());

			configureSaveColumnWidths(table, uniqueTableId);
			configureSaveColumnOrder(table, uniqueTableId);
			configureSaveColumnVisible(table, uniqueTableId);
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	private void configureSaveColumnWidths(final Table table, final String uniqueTableId)
	{
		final String keyStub = uniqueTableId + "-width";

		for (HeadingToPropertyId<E> id : getColumns())
		{
			final String setWidth = UserSettingsStorageFactory.getUserSettingsStorage()
					.get(keyStub + "-" + id.getPropertyId());
			if (setWidth != null && setWidth.length() > 0)
			{
				table.setColumnWidth(id.getPropertyId(), Integer.parseInt(setWidth));
			}
		}

		table.addColumnResizeListener(new ColumnResizeListener()
		{
			private static final long serialVersionUID = 4034036880290943146L;

			@Override
			public void columnResize(ColumnResizeEvent event)
			{
				final String propertyId = (String) event.getPropertyId();
				final int width = event.getCurrentWidth();
				UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + propertyId, "" + width);
			}
		});
	}

	private void configureSaveColumnOrder(final Table table, final String uniqueTableId)
	{
		final String keyStub = uniqueTableId + "-order";

		final Object[] availableColumns = table.getVisibleColumns();
		final String columns = UserSettingsStorageFactory.getUserSettingsStorage().get(keyStub);
		if (availableColumns.length > 0 && columns != null && !columns.isEmpty())
		{
			final Object[] parsedColumns = columns.replaceAll("\\[|\\]", "").split(", ?");
			if (parsedColumns.length > 0)
			{
				table.setVisibleColumns(calculateColumnOrder(availableColumns, parsedColumns));
			}
		}

		table.addColumnReorderListener(new ColumnReorderListener()
		{
			private static final long serialVersionUID = -2810298692555333890L;

			@Override
			public void columnReorder(ColumnReorderEvent event)
			{
				final Object[] columns = ((Table) event.getSource()).getVisibleColumns();
				UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub, "" + Arrays.toString(columns));
			}
		});
	}

	/**
	 * If a column order has already been saved for a user, but the columns for
	 * a table have been modified, then we need to remove any columns that no
	 * longer exist and add any new columns to the list of visible columns.
	 *
	 * @param availableColumns
	 *            the columns that are available in the table
	 * @param parsedColumns
	 *            the column order that has been restored from preferences
	 * @return the calculated order of columns with old removed and new added
	 */
	private Object[] calculateColumnOrder(final Object[] availableColumns, final Object[] parsedColumns)
	{
		final List<Object> availableList = new ArrayList<>(Arrays.asList(availableColumns));
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

	private void configureSaveColumnVisible(final Table table, final String uniqueTableId)
	{
		final String keyStub = uniqueTableId + "-visible";

		for (HeadingToPropertyId<E> id : getColumns())
		{
			final String setVisible = UserSettingsStorageFactory.getUserSettingsStorage()
					.get(keyStub + "-" + id.getPropertyId());
			if (setVisible != null && !setVisible.isEmpty())
				table.setColumnCollapsed(id.getPropertyId(), !Boolean.parseBoolean(setVisible));
		}

		table.addColumnCollapseListener(new ColumnCollapseListener()
		{
			private static final long serialVersionUID = 8903793320816698902L;

			@Override
			public void columnCollapseStateChange(ColumnCollapseEvent event)
			{
				final String propertyId = (String) event.getPropertyId();
				final boolean isVisible = !table.isColumnCollapsed(propertyId);
				UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + propertyId, "" + isVisible);
			}
		});
	}

	@Override
	public String toString()
	{
		return Arrays.toString(cols.toArray());
	}
}
