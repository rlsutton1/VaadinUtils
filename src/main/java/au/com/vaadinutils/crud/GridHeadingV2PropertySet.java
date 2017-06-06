package au.com.vaadinutils.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.viritin.grid.GeneratedPropertyListContainer;

import com.ejt.vaadin.sizereporter.ComponentResizeEvent;
import com.ejt.vaadin.sizereporter.ComponentResizeListener;
import com.ejt.vaadin.sizereporter.SizeReporter;
import com.google.common.base.Preconditions;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToBooleanConverter;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.CellReference;
import com.vaadin.ui.Grid.CellStyleGenerator;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.ColumnReorderEvent;
import com.vaadin.ui.Grid.ColumnReorderListener;
import com.vaadin.ui.Grid.ColumnResizeEvent;
import com.vaadin.ui.Grid.ColumnResizeListener;
import com.vaadin.ui.Grid.ColumnVisibilityChangeEvent;
import com.vaadin.ui.Grid.ColumnVisibilityChangeListener;
import com.vaadin.ui.Grid.MultiSelectionModel;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.fields.contextmenu.ButtonContextMenu;
import au.com.vaadinutils.fields.contextmenu.EntityContextMenu;
import au.com.vaadinutils.user.UserSettingsStorageFactory;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;

public class GridHeadingV2PropertySet<E> implements GridHeadingPropertySetIfc<E>
{
	private Logger logger = LogManager.getLogger();
	private List<GridHeadingV2ToPropertyId> cols = new LinkedList<>();
	private boolean eraseSavedConfig = false;
	private Grid grid;
	private String uniqueId;
	private boolean dynamicColumnWidth = true;
	private Map<String, WidthType> columnWidthTypes = new HashMap<>();
	private boolean actionColumnEnabled = true;
	private ActionMenuSetupProvider<E> actionMenuProvider;

	// Set to true if you would like to defer loading settings until
	// applySettingsToColumns is called
	private boolean deferLoadSettings = false;

	public GridHeadingV2PropertySet(final List<GridHeadingV2ToPropertyId> cols)
	{
		this.cols = cols;
	}

	public static <E> Builder<E> getBuilder()
	{
		return new Builder<>();
	}

	interface Start<E>
	{
		public AddingColumn<E> addColumn(String heading, String propertyId);

		public <T> AddingColumn<E> addColumn(String heading, SingularAttribute<E, T> headingPropertyId);

		public GridHeadingV2PropertySet<E> build();
	}

	public interface AddingColumn<E>
	{
		public AddingColumn<E> setVisible(boolean visible);

		public AddingColumn<E> setVisible(boolean visible, boolean locked);

		public AddingColumn<E> setWidth(Integer width);

		public AddingColumn<E> setWidth(Integer width, WidthType widthType);

		public AddingColumn<E> setColumnGenerator(PropertyValueGenerator<?> columnGenerator);

		public AddingColumn<E> setConverter(Converter<String, ?> converter);

		public Builder<E> addColumn();

	}

	public enum WidthType
	{
		// FREE: No explicit width is defined. The column can be resized by both
		// a dynamic operation (grid resize or another column being resized) and
		// by the user and the new width will be saved in both cases. FREE is
		// the default if no width is set.
		// INITIAL: Set with an initial width size. The column won't be resized
		// by a dynamic operation. The column can still be resized by the
		// user and the new width will be saved. INITIAL is the default if a
		// width is set.
		// FIXED: Set with a fixed width size. The column cannot be resized by
		// a dynamic operation, nor by the user.
		FREE, INITIAL, FIXED;
	}

	public static class Builder<E> implements AddingColumn<E>, Start<E>
	{
		private List<GridHeadingV2ToPropertyId> cols = new LinkedList<>();
		private boolean eraseSavedConfig = false;
		private boolean dynamicColumnWidth = true;
		private boolean actionColumnEnabled = true;
		private ActionMenuSetupProvider<E> actionMenuProvider;
		private GridHeadingV2ToPropertyId.Builder columnBuilder = null;

		@Override
		public GridHeadingV2PropertySet<E> build()
		{
			addColumn();
			final GridHeadingV2PropertySet<E> propertySet = new GridHeadingV2PropertySet<>(this.cols);
			propertySet.eraseSavedConfig = eraseSavedConfig;
			propertySet.dynamicColumnWidth = dynamicColumnWidth;
			propertySet.actionColumnEnabled = actionColumnEnabled;
			propertySet.actionMenuProvider = actionMenuProvider;

			return propertySet;
		}

		/**
		 * Dynamic column width ensures that the total width of columns is
		 * always equal to the width of the component. This prevents horizontal
		 * scrolling as well as ensures that the complete width of the component
		 * is always utilised. If a column is resized, then all columns to the
		 * right of it are also resized based on how much space there is
		 * remaining to the edge of the component. Dynamic column width is
		 * enabled by default.
		 */
		public void setDynamicColumnWidth(final boolean enabled)
		{
			dynamicColumnWidth = enabled;
		}

		public void setActionColumnEnabled(final boolean enabled)
		{
			actionColumnEnabled = enabled;
		}

		public void setActionMenuSetupProvider(final ActionMenuSetupProvider<E> actionMenuProvider)
		{
			this.actionMenuProvider = actionMenuProvider;
		}

		@Override
		public AddingColumn<E> setConverter(Converter<String, ?> converter)
		{
			columnBuilder.setConverter(converter);
			return this;
		}

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

		@Override
		public AddingColumn<E> addColumn(String heading, String propertyId)
		{
			addColumn();
			columnBuilder = new GridHeadingV2ToPropertyId.Builder(heading, propertyId);

			return this;
		}

		@Override
		public <T> AddingColumn<E> addColumn(String heading, SingularAttribute<E, T> headingPropertyId)
		{
			return addColumn(heading, headingPropertyId.getName());
		}

		public AddingColumn<E> addColumn(String heading)
		{
			addColumn();
			columnBuilder = new GridHeadingV2ToPropertyId.Builder(heading, null);

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
		public AddingColumn<E> setVisible(final boolean visible)
		{
			return setVisible(visible, false);
		}

		@Override
		public AddingColumn<E> setVisible(final boolean visible, final boolean locked)
		{
			columnBuilder.setVisible(visible, locked);
			return this;
		}

		@Override
		public AddingColumn<E> setWidth(final Integer width)
		{
			return setWidth(width, WidthType.INITIAL);
		}

		@Override
		public AddingColumn<E> setWidth(final Integer width, final WidthType widthType)
		{
			columnBuilder.setWidth(width, widthType);
			return this;
		}

		@Override
		public AddingColumn<E> setColumnGenerator(final PropertyValueGenerator<?> columnGenerator)
		{
			columnBuilder.setColumnGenerator(columnGenerator);
			return this;
		}

		public void setEraseSavedConfig()
		{
			eraseSavedConfig = true;
		}
	}

	public List<GridHeadingV2ToPropertyId> getColumns()
	{
		return cols;
	}

	private static final String ACTION_MENU_PROPERTY_ID = "_actionMenu";

	@SuppressWarnings("unchecked")
	private void addActionColumn()
	{
		((GeneratedPropertyListContainer<E>) grid.getContainerDataSource())
				.addGeneratedProperty(ACTION_MENU_PROPERTY_ID, getActionMenuColumnGenerator());
		final Column actionMenuColumn = grid.addColumn(ACTION_MENU_PROPERTY_ID);
		grid.setFrozenColumnCount(1);
		actionMenuColumn.setHeaderCaption("");
		actionMenuColumn.setRenderer(new ComponentRenderer());
		actionMenuColumn.setWidth(40);
		actionMenuColumn.setResizable(false);
		grid.setCellStyleGenerator(new CellStyleGenerator()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getStyle(CellReference event)
			{
				String style = "";
				if (event.getPropertyId().equals(ACTION_MENU_PROPERTY_ID))
				{
					style = "grid-actionmenu";
				}

				return style;
			}
		});
	}

	private PropertyValueGenerator<Component> getActionMenuColumnGenerator()
	{

		return new PropertyValueGenerator<Component>()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public Component getValue(Item item, Object itemId, Object propertyId)
			{
				final Button actionMenuButton = new Button(FontAwesome.ELLIPSIS_H);
				actionMenuButton.addStyleName(ValoTheme.BUTTON_QUIET);
				actionMenuButton.addStyleName(ValoTheme.BUTTON_SMALL);

				final ButtonContextMenu<E> contextMenu = new ButtonContextMenu<>();
				actionMenuProvider.setup(contextMenu);
				contextMenu.setAsButtonContextMenu(actionMenuButton, (E) itemId);
				actionMenuButton.addClickListener(new ClickListener()
				{
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event)
					{
						contextMenu.openContext(event);
					}
				});

				return actionMenuButton;
			}

			@Override
			public Class<Component> getType()
			{
				return Component.class;
			}
		};
	}

	public interface ActionMenuSetupProvider<T>
	{
		void setup(final EntityContextMenu<T> contextMenu);
	}

	/**
	 *
	 * @param grid
	 * @param uniqueId
	 *            - an id for this layout/grid combination, it is used to
	 *            identify stored column widths in a key value map
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void applyToGrid(final Class<E> entityClass, final Grid grid, final String uniqueId)
	{
		this.grid = grid;
		this.uniqueId = uniqueId;

		final List<String> columnsToShow = new LinkedList<>();

		try
		{
			// Avoid changing the container data source if we can
			Indexed gridContainer = grid.getContainerDataSource();
			if (actionColumnEnabled && actionMenuProvider != null)
			{
				gridContainer = wrapGridContainer(entityClass, grid);
				addActionColumn();
				columnsToShow.add(ACTION_MENU_PROPERTY_ID);
			}
			else
			{
				for (GridHeadingV2ToPropertyId column : getColumns())
				{
					if (column.isGenerated())
					{
						gridContainer = wrapGridContainer(entityClass, grid);
						break;
					}
				}
			}

			for (GridHeadingV2ToPropertyId column : getColumns())
			{
				final String propertyId = column.getPropertyId();
				if (column.isGenerated())
				{
					final PropertyValueGenerator<?> columnGenerator = column.getColumnGenerator();
					((GeneratedPropertyListContainer<E>) gridContainer).addGeneratedProperty(propertyId,
							columnGenerator);

					// If a column is added with the same name as a column that
					// already exists, then we shouldn't try to add it again
					if (grid.getColumn(propertyId) == null)
					{
						grid.addColumn(propertyId);
					}

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

				columnsToShow.add(propertyId);
				final Column gridColumn = grid.getColumn(propertyId);

				if (column.getRenderer() != null)
				{
					gridColumn.setRenderer(column.getRenderer(), null);
				}
				else if (gridContainer.getType(propertyId) == Boolean.class)
				{
					// Show a tick/cross if column is a boolean
					gridColumn.setRenderer(new HtmlRenderer(),
							new StringToBooleanConverter(FontAwesome.CHECK.getHtml(), FontAwesome.TIMES.getHtml()));
				}

				gridColumn.setHeaderCaption(column.getHeader());

				columnWidthTypes.put(propertyId, column.getWidthType());
				if (column.getWidth() != null)
				{
					setColumnWidth(gridColumn, column.getWidth());
					if (column.getWidthType().equals(WidthType.FIXED))
					{
						gridColumn.setResizable(false);
					}
				}
				else
				{
					gridColumn.setExpandRatio(1);
					gridColumn.setMinimumWidth(1);
				}

				if (column.isVisibilityLocked())
				{
					gridColumn.setHidable(false);
				}
				else
				{
					gridColumn.setHidable(true);
					if (!column.isVisible())
					{
						gridColumn.setHidden(true);
					}
				}
			}

			grid.setColumns(columnsToShow.toArray());

			if (eraseSavedConfig)
			{
				eraseSavedConfig(uniqueId);
			}

			if (!deferLoadSettings)
			{
				configureSaveColumnWidths(grid);
				configureSaveColumnOrder(grid);
				configureSaveColumnVisible(grid);
			}
		}
		catch (

		Exception e)
		{
			logger.error(e, e);
		}
	}

	private void eraseSavedConfig(final String uniqueTableId)
	{
		UserSettingsStorageFactory.getUserSettingsStorage().erase(uniqueTableId);
	}

	@SuppressWarnings("unchecked")
	private GeneratedPropertyListContainer<E> wrapGridContainer(final Class<E> entityClass, final Grid grid)
	{
		final Indexed gridContainer = grid.getContainerDataSource();
		if (gridContainer instanceof GeneratedPropertyListContainer)
		{
			return (GeneratedPropertyListContainer<E>) gridContainer;
		}

		final GeneratedPropertyListContainer<E> gplc = new GeneratedPropertyListContainer<>(entityClass);
		gplc.setCollection((Collection<E>) gridContainer.getItemIds());
		final Collection<?> containerPropertyIds = gridContainer.getContainerPropertyIds();
		if (!containerPropertyIds.isEmpty())
		{
			gplc.setContainerPropertyIds(containerPropertyIds.toArray(new String[containerPropertyIds.size()]));
		}
		grid.setContainerDataSource(gplc);

		return gplc;
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

		configureSaveColumnWidths(grid);
		configureSaveColumnOrder(grid);
		configureSaveColumnVisible(grid);
	}

	private void configureSaveColumnWidths(final Grid grid)
	{
		final String keyStub = uniqueId + "-width";

		for (GridHeadingV2ToPropertyId id : getColumns())
		{
			final String propertyId = id.getPropertyId();
			final String setting = keyStub + "-" + propertyId;
			final Double savedWidth = getSavedWidth(setting);
			if (savedWidth > 0)
			{
				if (!columnWidthTypes.get(propertyId).equals(WidthType.FIXED))
				{
					setColumnWidth(grid.getColumn(propertyId), savedWidth);
				}
			}
		}

		grid.addColumnResizeListener(new ColumnResizeListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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

	private Double getSavedWidth(final String setting)
	{
		final String savedWidth = UserSettingsStorageFactory.getUserSettingsStorage().get(setting);
		if (savedWidth == null || savedWidth.length() == 0)
		{
			return -1D;
		}

		Double width = -1D;
		try
		{
			width = Double.parseDouble(savedWidth);
		}
		catch (NumberFormatException e)
		{
			logger.error("Invalid width setting for " + setting);
		}

		return width;
	}

	private boolean isColumnResizable(final Column column)
	{
		if (!column.isResizable())
		{
			return false;
		}

		boolean resizable = true;
		final WidthType widthType = columnWidthTypes.get(column.getPropertyId());
		if (widthType != null)
		{
			switch (widthType)
			{
			case FREE:
				resizable = true;
				break;
			case INITIAL:
				// If there is a saved width for the column then INITIAL no
				// longer applies
				final String setting = uniqueId + "-width-" + column.getPropertyId();
				final Double savedWidth = getSavedWidth(setting);
				resizable = (savedWidth > 0);
				break;
			case FIXED:
				resizable = false;
				break;
			}
		}

		return resizable;
	}

	private void configureDynamicColumnWidth()
	{
		final AtomicBoolean resizing = new AtomicBoolean(false);
		final AtomicInteger gridWidth = new AtomicInteger();
		final SizeReporter sizeReporter = new SizeReporter(grid);

		// Grid resized
		sizeReporter.addResizeListener(new ComponentResizeListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void sizeChanged(ComponentResizeEvent event)
			{
				gridWidth.set(event.getWidth());

				if (gridWidth.get() > 1 && !grid.getColumns().isEmpty())
				{
					final List<Column> newColumnsToResize = new ArrayList<>();
					final List<Column> columnsToResize = new ArrayList<>();
					double columnsToResizeTotalWidth = 0;
					double fixedColumnsTotalWidth = 0;
					for (Column column : grid.getColumns())
					{
						if (column.isHidden())
						{
							continue;
						}
						else if (isColumnResizable(column))
						{
							if (column.getWidth() == -1)
							{
								newColumnsToResize.add(column);
							}
							else
							{
								columnsToResize.add(column);
								columnsToResizeTotalWidth += column.getWidth();
							}
						}
						else
						{
							fixedColumnsTotalWidth += column.getWidth();
						}
					}

					final int sizeForNewColumns = gridWidth.get() / grid.getColumns().size();
					for (Column column : newColumnsToResize)
					{
						resizing.set(true);
						setColumnWidth(column, sizeForNewColumns);
						resizing.set(false);
					}

					if (grid.getSelectionModel() instanceof MultiSelectionModel)
					{
						// in multi-select mode we need to allocate some space
						// for the selector
						fixedColumnsTotalWidth += 50;
					}

					final double widthDiscrepancy = gridWidth.get() - columnsToResizeTotalWidth - fixedColumnsTotalWidth
							- (newColumnsToResize.size() * sizeForNewColumns);

					if (widthDiscrepancy != 0)
					{
						// Get the total width of all rows to be resized
						double totalWidthOfColumnsToResize = 0;
						for (Column column : columnsToResize)
						{
							totalWidthOfColumnsToResize += column.getWidth();
						}

						// Calculate the percentage of space that each row takes
						// up
						// relative to all rows to be resized, take that
						// percentage from widthDiscrepancy and modify the width
						// of the column by that amount
						for (Column column : columnsToResize)
						{
							final double trimPercentage = column.getWidth() / totalWidthOfColumnsToResize;
							final double trimWidth = widthDiscrepancy * trimPercentage;

							resizing.set(true);
							setColumnWidth(column, column.getWidth() + trimWidth);
							resizing.set(false);
						}
					}
				}
			}
		});

		// Column resized
		grid.addColumnResizeListener(new ColumnResizeListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void columnResize(ColumnResizeEvent event)
			{
				if (!resizing.get() && gridWidth.get() > 0)
				{
					resizing.set(true);

					final List<Column> gridColumns = grid.getColumns();
					final int totalColumns = gridColumns.size();
					final Column resizedColumn = event.getColumn();
					final double resizedColumnWidth = resizedColumn.getWidth();
					final int resizedColumnIndex = gridColumns.indexOf(resizedColumn);
					final List<Column> columnsToResize = new ArrayList<>();
					double fixedColumnsTotalWidth = 0;

					// Get columns to the right of the resized column that will
					// be
					// resized and total width of columns that won't
					for (int i = resizedColumnIndex + 1; i < totalColumns; i++)
					{
						final Column column = gridColumns.get(i);
						if (column.isHidden())
						{
							continue;
						}
						else if (column.isResizable())
						{
							columnsToResize.add(column);
						}
						else
						{
							fixedColumnsTotalWidth += column.getWidth();
						}
					}

					// availableWidth = grid width - width of column being
					// resized - columns to the right that won't be resized -
					// widths
					// of columns to the left
					double availableWidth = gridWidth.get() - resizedColumnWidth - fixedColumnsTotalWidth;
					for (int i = 0; i < resizedColumnIndex; i++)
					{
						final Column column = gridColumns.get(i);
						if (column.isHidden())
						{
							continue;
						}
						availableWidth -= column.getWidth();
					}

					// If there is width available for resizing, but there are
					// no
					// columns to resize, then assign it back to the column we
					// resized (this will happen if there are only non-resizable
					// columns to the right of the column we are resizing)
					if (availableWidth != 0 && columnsToResize.size() == 0)
					{
						setColumnWidth(resizedColumn, resizedColumn.getWidth() + availableWidth);
					}
					// Otherwise resize columns to the right
					else
					{
						final double perColumnWidth = availableWidth / columnsToResize.size();
						for (Column column : columnsToResize)
						{
							setColumnWidth(column, perColumnWidth);
						}

					}

					resizing.set(false);
				}
			}
		});

		// Column visibility toggled
		grid.addColumnVisibilityChangeListener(new ColumnVisibilityChangeListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void columnVisibilityChanged(ColumnVisibilityChangeEvent event)
			{
				if (gridWidth.get() > 0)
				{
					resizing.set(true);

					final List<Column> gridColumns = grid.getColumns();
					final Column toggledColumn = event.getColumn();
					final List<Column> columnsToResize = new ArrayList<>();
					double columnsToResizeTotalWidth = 0;
					double fixedColumnsTotalWidth = 0;

					for (Column column : gridColumns)
					{
						if (column.equals(toggledColumn) || column.isHidden())
						{
							continue;
						}
						else if (isColumnResizable(column))
						{
							columnsToResize.add(column);
							columnsToResizeTotalWidth += column.getWidth();
						}
						else
						{
							fixedColumnsTotalWidth += column.getWidth();
						}
					}

					// If toggled column has become visible, make room for it
					if (!toggledColumn.isHidden())
					{
						double newColumnWidth = 0;
						// Trim <100% / new visible column count> from each
						// column -
						// add them up and this becomes the width of the newly
						// visible column
						double trimPercentage = 1d / (columnsToResize.size() + 1);
						for (Column column : columnsToResize)
						{
							final double trimWidth = column.getWidth() * trimPercentage;
							setColumnWidth(column, column.getWidth() - trimWidth);
							newColumnWidth += trimWidth;
						}
						if (!(newColumnWidth == 0))
						{
							setColumnWidth(toggledColumn, newColumnWidth);
						}
					}
					// Otherwise fill up the newly created blank space
					else
					{
						final double widthDiscrepancy = gridWidth.get() - columnsToResizeTotalWidth
								- fixedColumnsTotalWidth;

						if (widthDiscrepancy != 0)
						{
							final double perColumnChange = widthDiscrepancy / columnsToResize.size();
							for (Column column : columnsToResize)

							{
								setColumnWidth(column, column.getWidth() + perColumnChange);
							}
						}
					}
				}

				resizing.set(false);
			}
		});
	}

	private void setColumnWidth(final Column column, final double width)
	{
		// Avoid IllegalArgumentException if proposed width ends up being < 0
		column.setWidth(width > 0 ? width : 0);
	}

	private void configureSaveColumnOrder(final Grid grid)
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void columnReorder(ColumnReorderEvent event)
			{
				final List<Column> reorderedColumns = ((Grid) event.getSource()).getColumns();
				if (reorderedColumns.size() > 0)
				{
					String parsedColumns = "";
					for (Column column : reorderedColumns)
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
	 *            the columns that are available in the grid
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
		// grid in
		final List<Object> newList = new ArrayList<>(availableList);
		newList.removeAll(parsedList);
		for (Object column : newList)
		{
			parsedList.add(availableList.indexOf(column), column);
		}

		return parsedList.toArray();
	}

	private void configureSaveColumnVisible(final Grid grid)
	{
		final String keyStub = uniqueId + "-visible";

		for (GridHeadingV2ToPropertyId id : getColumns())
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
