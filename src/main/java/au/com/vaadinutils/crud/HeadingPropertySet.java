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
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.ColumnResizeListener;

public class HeadingPropertySet<E>
{
	private List<HeadingToPropertyId<E>> cols = new LinkedList<HeadingToPropertyId<E>>();

	Logger logger = LogManager.getLogger();

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
		List<HeadingToPropertyId<E>> cols = new LinkedList<HeadingToPropertyId<E>>();

		public HeadingPropertySet<E> build()
		{
			HeadingPropertySet<E> tmp = new HeadingPropertySet<E>();
			tmp.cols = this.cols;
			return tmp;
		}

		public Builder<E> addGeneratedColumn(String heading, ColumnGenerator columnGenerator)
		{
			addGeneratedColumn(heading, heading + "-generated", columnGenerator);
			return this;

		}

		public Builder<E> addGeneratedColumn(String heading, ColumnGenerator columnGenerator, int width)
		{
			addGeneratedColumn(heading, heading + "-generated", columnGenerator, width);
			return this;

		}

		/**
		 * @param heading
		 * @param headingPropertyId
		 * @param columnGenerator
		 * @return
		 */
		public Builder<E> addGeneratedColumn(String heading, String headingPropertyId, ColumnGenerator columnGenerator)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, columnGenerator));
			return this;

		}

		public Builder<E> addGeneratedColumn(String heading, String headingPropertyId, ColumnGenerator columnGenerator,
				int width)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, columnGenerator).setWidth(width));
			return this;

		}

		public Builder<E> addHiddenColumn(String heading, String headingPropertyId)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null).setHidden());
			return this;

		}

		public Builder<E> addGeneratedHiddenColumn(String heading, ColumnGenerator columnGenerator)
		{
			cols.add(new HeadingToPropertyId<E>(heading, heading + "-generated", columnGenerator).setHidden());
			return this;

		}

		public Builder<E> addGeneratedHiddenColumn(String heading, String headingPropertyId,
				ColumnGenerator columnGenerator)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, columnGenerator).setHidden());
			return this;

		}

		public Builder<E> addGeneratedHiddenColumn(String heading, String headingPropertyId,
				ColumnGenerator columnGenerator, int width)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, columnGenerator).setHidden()
					.setWidth(width));
			return this;

		}

		public Builder<E> addColumn(String heading, String headingPropertyId)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null));
			return this;

		}

		public <T extends Object> Builder<E> addColumn(String heading, Path pathToHeadingPropertyId)
		{
			cols.add(new HeadingToPropertyId<E>(heading, pathToHeadingPropertyId.getName(), null));
			return this;

		}

		public <T extends Object> Builder<E> addColumn(String heading, SingularAttribute<E, T> headingPropertyId)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId.getName(), null));
			return this;

		}

		public <T extends Object> Builder<E> addColumn(String heading, SingularAttribute<E, T> headingPropertyId,
				int width)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId.getName(), null).setWidth(width));
			return this;

		}

		public <T extends Object> Builder<E> addHiddenColumn(String heading, SingularAttribute<E, T> headingPropertyId,
				int width)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId, null, true).setWidth(width));
			return this;

		}

		/**
		 * @param heading
		 * @param headingPropertyId
		 * @param columnGenerator
		 * @return
		 */
		public <T extends Object> Builder<E> addGeneratedColumn(String heading,
				SingularAttribute<E, T> headingPropertyId, ColumnGenerator columnGenerator)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId.getName(), columnGenerator));
			return this;

		}

		/**
		 * @param heading
		 * @param headingPropertyId
		 * @param columnGenerator
		 * @return
		 */
		public <T extends Object> Builder<E> addGeneratedColumn(String heading,
				SingularAttribute<E, T> headingPropertyId, ColumnGenerator columnGenerator, int width)
		{
			cols.add(new HeadingToPropertyId<E>(heading, headingPropertyId.getName(), columnGenerator).setWidth(width));
			return this;

		}

	}

	public List<HeadingToPropertyId<E>> getColumns()
	{

		return cols;
	}

	public void applyToTable(Table table)
	{
		StackTraceElement[] trace = new Exception().getStackTrace();
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
	public void applyToTable(Table table, String uniqueTableId)
	{
		try
		{
			List<String> colsToShow = new LinkedList<String>();
			for (HeadingToPropertyId<E> column : getColumns())
			{
				colsToShow.add(column.getPropertyId());
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
				table.setColumnHeader(column.getPropertyId(), column.getHeader());
				if (column.getWidth() != null)
				{
					table.setColumnWidth(column.getPropertyId(), column.getWidth());
				}

				if (column.isHidden())
				{
					table.setColumnCollapsingAllowed(true);
					table.setColumnCollapsed(column.getPropertyId(), true);

				}

			}
			table.setVisibleColumns(colsToShow.toArray());

			configureSaveColumnWidths(table, uniqueTableId);
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	private void configureSaveColumnWidths(Table table, final String uniqueTableId)
	{
		final String keyStub = uniqueTableId + "-width";

		for (HeadingToPropertyId<E> id : getColumns())
		{
			String setWidth = UserSettingsStorageFactory.getUserSettingsStorage().get(
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
				String propertyId = (String) event.getPropertyId();
				int width = event.getCurrentWidth();
				UserSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + propertyId, "" + width);

			}
		});

	}

	public String toString()
	{
		return Arrays.toString(cols.toArray());
	}
}
