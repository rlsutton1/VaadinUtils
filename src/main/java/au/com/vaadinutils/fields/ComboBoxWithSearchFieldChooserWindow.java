package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.sort.Sort;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.GridHeadingPropertySet;
import au.com.vaadinutils.crud.GridHeadingPropertySet.Builder;
import au.com.vaadinutils.crud.GridHeadingPropertySetIfc;
import au.com.vaadinutils.crud.SearchableGrid;
import au.com.vaadinutils.dao.JpaBaseDao;

public class ComboBoxWithSearchFieldChooserWindow<T extends CrudEntity, C extends Indexed & Filterable>
{

	private String caption;
	private ChooserListener listener;
	private SearchableGrid<T, C> grid;
	private C localContainer;

	Builder<T> builder = new GridHeadingPropertySet.Builder<>();
	private Class<? extends T> type;
	private GridHeadingPropertySetIfc<T> headingProps;
	private VerticalLayout holder;
	private String[] sortColumns;

	Logger logger = LogManager.getLogger();

	public interface IndexedAndFilterable extends Indexed, Filterable
	{

	}

	@SuppressWarnings("unchecked")
	public ComboBoxWithSearchFieldChooserWindow(final ChooserListener listener, Class<? extends T> type,
			final String caption, C container, Builder<T> headingBuilder, String[] sortColumns)
	{

		this.caption = caption;
		this.listener = listener;
		this.type = type;
		this.sortColumns = sortColumns;
		if (sortColumns == null || sortColumns.length == 0)
		{
			this.sortColumns = new String[] { "name" };
		}

		if (container != null)
		{
			this.localContainer = container;
		}
		else
		{
			this.localContainer = (C) JpaBaseDao.getGenericDao(type).createVaadinContainer();
		}

		if (headingBuilder != null)
		{
			builder = headingBuilder;
		}
		else
		{
			builder.createColumn("Action", "name").setLockedState(true).setWidth(100).addColumn().build();

		}
		headingProps = builder.build();

	}

	@SuppressWarnings("unchecked")
	public Component getPopupContent2()
	{

		grid = new SearchableGrid<T, C>()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public GridHeadingPropertySetIfc<T> getHeadingPropertySet()
			{

				return headingProps;
			}

			@Override
			protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
			{
				List<Filter> filters = new ArrayList<>();

				if (filterString.length() > 0)
				{

					filters.add(new SimpleStringFilter(sortColumns[0], filterString, true, false));

				}

				if (filters.size() == 0)
				{
					return null;
				}

				return new And(filters.toArray(new Filter[] {}));
			}

			@Override
			protected String getTitle()
			{
				return null;
			}

			@Override
			public C getContainer()
			{

				return localContainer;

			}

		};

		grid.init(caption, (Class<T>) type);
		// grid.getGrid().removeHeaderRow(0);

		ItemClickListener itemClickListener = new ItemClickListener()
		{

			private static final long serialVersionUID = -4659382639878762429L;

			@Override
			public void itemClick(ItemClickEvent event)
			{
				listener.selected(event.getItemId());

			}

		};

		Sort sort = Sort.by(sortColumns[0]);
		for (int i = 1; i < sortColumns.length; i++)
		{

			sort.then(sortColumns[i]);
		}

		grid.sort(sort);

		grid.setSelectionMode(SelectionMode.SINGLE);

		grid.addItemClickListener(itemClickListener);
		grid.setSizeFull();

		holder = new VerticalLayout();
		holder.setWidth("380");
		holder.setHeight("300");
		holder.addComponent(grid);

		return holder;
	}

	void select(T currentValue)
	{
		if (currentValue != null)
		{
			Object id = currentValue.getId();
			if (!grid.getContainerDataSource().containsId(id))
			{
				id = currentValue;
			}
			if (grid.getContainerDataSource().containsId(id))
			{
				grid.select(id);
				grid.getGrid().scrollTo(id);
			}
		}

	}

	public void setContainerFilters(Filter filter)
	{
		localContainer.removeAllContainerFilters();
		localContainer.addContainerFilter(filter);

	}

	public void setNullSelectionAllowed(boolean b, String prompt)
	{
		Button noneButton = new Button(StringUtils.defaultString(prompt, "Select None"));
		noneButton.setWidth("100%");
		noneButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		noneButton.addClickListener(new ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				listener.selected(null);
				grid.select(null);

			}
		});
		grid.addComponent(noneButton);
	}

	public boolean containerContains(Object id)
	{

		return grid.getContainerDataSource().containsId(id);
	}

}
