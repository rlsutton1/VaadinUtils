package au.com.vaadinutils.crud;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.vaadin.teemusa.gridextensions.refresher.GridRefresher;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.sort.Sort;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.CellDescriptionGenerator;
import com.vaadin.ui.Grid.CellStyleGenerator;
import com.vaadin.ui.Grid.RowStyleGenerator;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SelectionModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.crud.security.SecurityManagerFactoryProxy;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.menu.Menu;
import au.com.vaadinutils.menu.Menus;

public abstract class SearchableGrid<E, T extends Indexed & Filterable> extends CustomComponent
{
	private static final long serialVersionUID = 1L;

	private boolean initialised;
	private TextField searchField = new TextField();
	private AbstractLayout advancedSearchLayout;
	private AbstractLayout searchBar;
	private Button advancedSearchButton;
	private boolean advancedSearchOn = false;
	private Grid grid;
	private T container;
	private String filterString = "";
	private GridHeadingPropertySet headingPropertySet;

	public SearchableGrid(String uniqueId)
	{
		if (!getSecurityManager().canUserView())
		{
			this.setSizeFull();
			this.setCompositionRoot(new Label("Sorry, you do not have permission to access " + getTitle()));
			return;
		}

		container = getContainer();
		grid = new Grid(new GeneratedPropertyContainer(container));
		grid.setSizeFull();
		searchBar = buildSearchBar();
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		addTitle(layout);
		layout.addComponent(searchBar);
		layout.addComponent(grid);
		layout.setExpandRatio(grid, 1);
		this.setCompositionRoot(layout);
		headingPropertySet = getHeadingPropertySet();
		headingPropertySet.setDeferLoadSettings(true);
		headingPropertySet.applyToGrid(grid, uniqueId);
	}

	private void addTitle(final VerticalLayout layout)
	{
		final String titleText = getTitle();
		if (titleText != null && !titleText.isEmpty())
		{
			final Label titleLabel = new Label(getTitle());
			titleLabel.setStyleName(setTitleStyleName());
			layout.addComponent(titleLabel);
		}
	}

	@Override
	public void beforeClientResponse(boolean initial)
	{
		super.beforeClientResponse(initial);
		if (!initialised)
		{
			headingPropertySet.applySettingsToColumns();
			initialised = true;
		}
	}

	abstract public GridHeadingPropertySet getHeadingPropertySet();

	abstract public T getContainer();

	protected String getTitle()
	{
		Annotation annotation = this.getClass().getAnnotation(Menu.class);
		if (annotation instanceof Menu)
		{
			return ((Menu) annotation).display();
		}
		annotation = this.getClass().getAnnotation(Menus.class);
		if (annotation instanceof Menus)
		{
			return ((Menus) annotation).menus()[0].display();
		}

		return "Override getTitle() to set a custom title.";
	}

	private CrudSecurityManager getSecurityManager()
	{
		return SecurityManagerFactoryProxy.getSecurityManager(this.getClass());
	}

	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn)
	{
		// grid.addGeneratedColumn(id, generatedColumn);
	}

	protected AbstractLayout buildSearchBar()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		searchField.setWidth(100, Unit.PERCENTAGE);
		searchBar = layout;

		final HorizontalLayout basicSearchLayout = new HorizontalLayout();
		basicSearchLayout.setWidth(100, Unit.PERCENTAGE);
		basicSearchLayout.setSpacing(true);
		layout.addComponent(basicSearchLayout);

		final AbstractLayout advancedSearch = buildAdvancedSearch();
		if (advancedSearch != null)
		{
			basicSearchLayout.addComponent(advancedSearchButton);
		}

		searchField.setInputPrompt("Search");
		searchField.setId("searchField");
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		searchField.setImmediate(true);
		searchField.addTextChangeListener(new TextChangeListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void textChange(final TextChangeEvent event)
			{
				filterString = event.getText().trim();
				triggerFilter(filterString);
			}

		});

		// clear button
		final Button clear = createClearButton();
		basicSearchLayout.addComponent(clear);
		basicSearchLayout.setComponentAlignment(clear, Alignment.MIDDLE_CENTER);
		basicSearchLayout.addComponent(searchField);
		basicSearchLayout.setExpandRatio(searchField, 1.0f);
		basicSearchLayout.setSpacing(true);

		searchField.focus();

		return layout;
	}

	private Button createClearButton()
	{

		final Button clear = new Button("X");
		clear.setStyleName(Reindeer.BUTTON_SMALL);
		clear.setImmediate(true);
		clear.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				searchField.setValue("");
				clearAdvancedFilters();
				triggerFilter();
			}
		});
		return clear;
	}

	private AbstractLayout buildAdvancedSearch()
	{
		advancedSearchLayout = getAdvancedSearchLayout();
		if (advancedSearchLayout != null)
		{
			advancedSearchButton = new Button(getAdvancedCaption());
			advancedSearchOn = false;

			advancedSearchButton.setImmediate(true);
			advancedSearchButton.addClickListener(new ClickListener()
			{
				private static final long serialVersionUID = 7777043506655571664L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					clearAdvancedFilters();
					advancedSearchOn = !advancedSearchOn;
					advancedSearchLayout.setVisible(advancedSearchOn);
					if (!advancedSearchOn)
					{
						triggerFilter();
					}
					if (!advancedSearchOn)
					{
						advancedSearchButton.setCaption(getAdvancedCaption());
						advancedSearchButton.removeStyleName(ValoTheme.BUTTON_FRIENDLY);
					}
					else
					{
						advancedSearchButton.setCaption(getBasicCaption());
						advancedSearchButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
					}
				}
			});

			searchBar.addComponent(advancedSearchLayout);
			advancedSearchLayout.setVisible(false);
		}

		return advancedSearchLayout;
	}

	protected AbstractLayout getAdvancedSearchLayout()
	{
		return null;
	}

	public void triggerFilter()
	{
		triggerFilter(searchField.getValue());
	}

	protected void triggerFilter(String searchText)
	{
		boolean advancedSearchActive = advancedSearchOn;
		Filter filter = getContainerFilter(searchText, advancedSearchActive);
		if (filter == null)
		{
			resetFilters();
		}
		else
		{
			applyFilter(filter);
		}
	}

	protected void resetFilters()
	{
		container.removeAllContainerFilters();
	}

	protected void applyFilter(Filter filter)
	{
		resetFilters();
		container.addContainerFilter(filter);
	}

	public String getSearchFieldText()
	{
		return filterString;
	}

	abstract protected Filter getContainerFilter(String filterString, boolean advancedSearchActive);

	protected void clearAdvancedFilters()
	{
	}

	public void addItemClickListener(ItemClickListener listener)
	{
		grid.addItemClickListener(listener);

	}

	public void addSelectionListener(SelectionListener listener)
	{
		grid.addSelectionListener(listener);

	}

	public void addStyleName(String style)
	{
		grid.addStyleName(style);
	}

	public void removeAllContainerFilters()
	{
		container.removeAllContainerFilters();

	}

	public void addContainerFilter(Filter filter)
	{
		container.addContainerFilter(filter);

	}

	public boolean select(Object itemId)
	{
		return grid.select(itemId);
	}

	public void deselectAll()
	{
		grid.deselectAll();
	}

	public SelectionModel getSelectionModel()
	{
		return grid.getSelectionModel();
	}

	public SelectionModel setSelectionMode(SelectionMode selectionMode)
	{
		return grid.setSelectionMode(selectionMode);
	}

	public Object getSelectedRow()
	{
		return grid.getSelectedRow();
	}

	public Collection<Object> getSelectedRows()
	{
		return grid.getSelectedRows();
	}

	public void setSearchFilterText(String text)
	{
		searchField.setValue(text);
		triggerFilter(text);
	}

	public AbstractLayout getSearchBar()
	{
		return searchBar;
	}

	public Grid getGrid()
	{
		return grid;
	}

	public void setColumnReorderingAllowed(boolean columnReorderingAllowed)
	{
		grid.setColumnReorderingAllowed(true);
	}

	public boolean isColumnReorderingAllowed()
	{
		return grid.isColumnReorderingAllowed();
	}

	public void setConverter(String propertyId, Converter<String, ?> converter)
	{
		grid.getColumn(propertyId).setConverter(converter);
	}

	public void refresh(final Object itemId)
	{
		GridRefresher.extend(grid).refresh(itemId);
	}

	public void refresh()
	{
		final GridRefresher refresher = GridRefresher.extend(grid);
		for (Object itemId : grid.getContainerDataSource().getItemIds())
		{
			refresher.refresh(itemId);
		}
	}

	public void refreshRows(Object... itemIds)
	{
		grid.refreshRows(itemIds);
	}

	@Override
	public void addContextClickListener(ContextClickListener listener)
	{
		grid.addContextClickListener(listener);
	}

	protected String getAdvancedCaption()
	{
		return "Advanced";
	}

	protected String getBasicCaption()
	{
		return "Basic";
	}

	public void setCellDescriptionGenerator(CellDescriptionGenerator generator)
	{
		grid.setCellDescriptionGenerator(generator);
	}

	public void setCellStyleGenerator(CellStyleGenerator generator)
	{
		grid.setCellStyleGenerator(generator);
	}

	public void setRowStyleGenerator(RowStyleGenerator generator)
	{
		grid.setRowStyleGenerator(generator);
	}

	public String setTitleStyleName()
	{
		return "";
	}

	public void sort(final Sort sort)
	{
		grid.sort(sort);
	}

}