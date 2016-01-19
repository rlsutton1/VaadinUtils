package au.com.vaadinutils.crud;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.Path;
import au.com.vaadinutils.user.UserSettingsStorageFactory;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnCollapseEvent;
import com.vaadin.ui.Table.ColumnCollapseListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnReorderEvent;
import com.vaadin.ui.Table.ColumnReorderListener;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.ColumnResizeListener;

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
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null, defaultVisibleState, lockedState, null));
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
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null, defaultVisibleState, lockedState, null));
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
	}

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
			final String setWidth = UserSettingsStorageFactory.getUserSettingsStorage().get(
					keyStub + "-" + id.getPropertyId());
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

		String columns = UserSettingsStorageFactory.getUserSettingsStorage().get(keyStub);
		if (columns != null && !columns.isEmpty())
		{
			columns = columns.replaceAll("\\[|\\]", "");
			final Object[] parsedColumns = columns.split(", ?");
			if (parsedColumns.length > 0)
				table.setVisibleColumns(parsedColumns);
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

	private void configureSaveColumnVisible(final Table table, final String uniqueTableId)
	{
		final String keyStub = uniqueTableId + "-visible";

		for (HeadingToPropertyId<E> id : getColumns())
		{
			final String setVisible = UserSettingsStorageFactory.getUserSettingsStorage().get(
					keyStub + "-" + id.getPropertyId());
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

	public String toString()
	{
		return Arrays.toString(cols.toArray());
	}
}
