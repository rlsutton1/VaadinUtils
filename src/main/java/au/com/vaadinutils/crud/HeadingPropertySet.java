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
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
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

public class HeadingPropertySet
{
	private List<HeadingToPropertyId> cols = new LinkedList<>();

	private Logger logger = LogManager.getLogger();

	public boolean autoExpandColumns = false;

	private boolean eraseSavedConfig = false;

	private HeadingPropertySet()
	{
		// use the builder!
	}

	public static <E> Builder<E> getBuilder(Class<E> Class)
	{
		return new Builder<>();
	}

	interface Start<E>
	{
		public AddingColumn<E> createColumn(String heading, String propertyId);

		public <T> AddingColumn<E> createColumn(String heading, SingularAttribute<E, T> headingPropertyId);

		public HeadingPropertySet build();
	}

	public interface AddingColumn<E>
	{

		public AddingColumn<E> setLockedState(boolean lockedState);

		public AddingColumn<E> setDefaultVisibleState(boolean defaultVisibleState);

		public AddingColumn<E> setWidth(Integer width);

		public AddingColumn<E> setColumnGenerator(ColumnGenerator columnGenerator);

	}

	public static class Builder<E> implements AddingColumn<E>, Start<E>
	{
		private List<HeadingToPropertyId> cols = new LinkedList<>();
		private boolean autoExpandColumns = false;
		private boolean eraseSavedConfig = false;

		@Override
		public HeadingPropertySet build()
		{
			addColumn();

			final HeadingPropertySet tmp = new HeadingPropertySet();
			tmp.cols = this.cols;
			tmp.autoExpandColumns = autoExpandColumns;
			tmp.eraseSavedConfig = eraseSavedConfig;

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
			cols.add(new HeadingToPropertyId(heading, headingPropertyId, null, defaultVisibleState, lockedState, null));
			return this;
		}

		HeadingToPropertyId.Builder columnBuilder = null;

		@Override
		public AddingColumn<E> createColumn(String heading, String propertyId)
		{

			addColumn();

			columnBuilder = new HeadingToPropertyId.Builder(heading, propertyId);
			return this;
		}

		@Override
		public <T> AddingColumn<E> createColumn(String heading, SingularAttribute<E, T> headingPropertyId)
		{

			addColumn();

			columnBuilder = new HeadingToPropertyId.Builder(heading, headingPropertyId.getName());
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
		public AddingColumn<E> setColumnGenerator(ColumnGenerator columnGenerator)
		{
			columnBuilder.setColumnGenerator(columnGenerator);
			return this;
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<? super E, T> headingPropertyId, final boolean defaultVisibleState,
				final boolean lockedState, int width)
		{
			return addColumn(heading, headingPropertyId.getName(), defaultVisibleState, lockedState, width);
		}

		public <T extends Object> Builder<E> addColumn(final String heading, final String headingPropertyId,
				final boolean defaultVisibleState, final boolean lockedState)
		{
			cols.add(new HeadingToPropertyId(heading, headingPropertyId, null, defaultVisibleState, lockedState, null));
			return this;
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<? super E, T> headingPropertyId, final boolean defaultVisibleState,
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
			cols.add(new HeadingToPropertyId(heading, headingPropertyId, columnGenerator, defaultVisibleState,
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
			cols.add(new HeadingToPropertyId(heading, headingPropertyId, columnGenerator, defaultVisibleState,
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
				final SingularAttribute<? super E, T> headingPropertyId, final int width)
		{
			return addColumn(heading, headingPropertyId, true, false, width);
		}

		public Builder<E> addColumn(final String heading, final String headingPropertyId)
		{
			return addColumn(heading, headingPropertyId, true, false);
		}

		public <T extends Object> Builder<E> addColumn(final String heading,
				final SingularAttribute<? super E, T> headingPropertyId)
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
		public Builder<E> addColumn(String headingLabel, SingularAttribute<? super E, Date> column, String dateFormat,
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

		public void setAutoExpandColumns()
		{
			autoExpandColumns = true;

		}

		public void setEraseSavedConfig()
		{
			eraseSavedConfig = true;
		}

		public Builder<E> addGeneratedBooleanIconColumn(String heading, final SingularAttribute<E, Boolean> attribute,
				final FontAwesome trueIcon, final FontAwesome falseIcon)
		{

			ColumnGenerator generator = new ColumnGenerator()
			{

				private static final long serialVersionUID = -7730752061513328598L;

				@Override
				public Object generateCell(Table source, Object itemId, Object columnId)
				{
					Boolean checked = (Boolean) source.getItem(itemId).getItemProperty(attribute.getName()).getValue();

					final Label label = new Label();
					if (checked != null)
					{
						label.setContentMode(ContentMode.HTML);
						if (checked)
						{
							if (trueIcon != null)
							{
								label.setValue(trueIcon.getHtml());
							}
						}
						else
						{
							if (falseIcon != null)
							{
								label.setValue(falseIcon.getHtml());
							}
						}
					}

					return label;

				}
			};

			addGeneratedColumn(heading, attribute, generator);

			return this;

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
		final private SimpleDateFormat sdfParse = new SimpleDateFormat("yyyy-MM-dd");
		final private String headingPropertyId;

		DateColumnGenerator(String headingPropertyId, String format)
		{
			this.headingPropertyId = headingPropertyId;
			this.sdf = new SimpleDateFormat(format);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId)
		{
			Item item = source.getItem(itemId);

			Object objDate = item.getItemProperty(headingPropertyId).getValue();

			String formattedDate = "";

			if (objDate instanceof Date)
			{
				formattedDate = sdf.format((Date) objDate);
			}
			else if (objDate != null)
			{
				String strDate = objDate.toString();
				try
				{
					formattedDate = sdf.format(sdfParse.parse(strDate));
				}
				catch (ParseException e)
				{
					// just so we have a value.
					formattedDate = "Invalid";
					logger.error(
							"Looks like our assumptions about the format of dates is wrong. Please update the parse format to match:"
									+ strDate + " " + sdf.toPattern());
				}
			}

			Label label = new Label(formattedDate);

			return label;
		}

	}

	public List<HeadingToPropertyId> getColumns()
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
			final List<String> colsToShow = new LinkedList<>();
			for (HeadingToPropertyId column : getColumns())
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
				else
				{
					if (autoExpandColumns)
					{
						table.setColumnExpandRatio(column.getPropertyId(), (float) (1.0 / getColumns().size()));
					}
				}

				if (!column.isVisibleByDefault())
				{
					table.setColumnCollapsingAllowed(true);
					table.setColumnCollapsed(column.getPropertyId(), true);
				}

				if (column.isLocked())
				{
					table.setColumnCollapsible(column.getPropertyId(), false);
				}

			}
			table.setVisibleColumns(colsToShow.toArray());
			if (eraseSavedConfig)
			{
				eraseSavedConfig(uniqueTableId);
			}

			configureSaveColumnWidths(table, uniqueTableId);
			configureSaveColumnOrder(table, uniqueTableId);
			configureSaveColumnVisible(table, uniqueTableId);
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

	private void configureSaveColumnWidths(final Table table, final String uniqueTableId)
	{
		final String keyStub = uniqueTableId + "-width";

		for (HeadingToPropertyId id : getColumns())
		{
			final String setWidth = UserSettingsStorageFactory.getUserSettingsStorage()
					.get(keyStub + "-" + id.getPropertyId());
			if (setWidth != null && setWidth.length() > 0)
			{
				try
				{
					table.setColumnWidth(id.getPropertyId(), Integer.parseInt(setWidth));
				}
				catch (Exception e)
				{
					logger.warn(e);
				}
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

		for (HeadingToPropertyId id : getColumns())
		{
			final String setVisible = UserSettingsStorageFactory.getUserSettingsStorage()
					.get(keyStub + "-" + id.getPropertyId());
			if (setVisible != null && !setVisible.isEmpty())
			{
				table.setColumnCollapsed(id.getPropertyId(), !Boolean.parseBoolean(setVisible));
			}
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
