package au.com.vaadinutils.crud;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.apache.logging.log4j.Logger;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.crud.security.SecurityManagerFactoryProxy;
import au.com.vaadinutils.fields.SelectionListener;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.menu.Menu;
import au.com.vaadinutils.menu.Menus;

public abstract class SearchableSelectableEntityTable<E> extends VerticalLayout
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	protected TextField searchField = new TextField();
	private AbstractLayout advancedSearchLayout;
	private AbstractLayout searchBar;
	private Button advancedSearchCheckbox;
	public boolean advancedSearchOn = false;
	protected SelectableEntityTable<E> selectableTable;
	protected Container.Filterable container;
	private String uniqueId;

	private String filterString = "";

	public SearchableSelectableEntityTable(String uniqueId)
	{
		this.uniqueId = uniqueId;
		container = getContainer();
		selectableTable = new SelectableEntityTable<E>(container, getHeadingPropertySet(), uniqueId);
		selectableTable.setSizeFull();
		this.setSizeFull();
		if (!getSecurityManager().canUserView())
		{
			this.setSizeFull();
			this.addComponent(new Label("Sorry, you do not have permission to access " + getTitle()));
			return;
		}

		searchBar = buildSearchBar();

		final String titleText = getTitle();
		if (titleText != null && !titleText.isEmpty())
		{
			Label title = new Label(getTitle());
			title.setStyleName(Reindeer.LABEL_H1);
			this.addComponent(title);
		}

		this.addComponent(searchBar);
		this.addComponent(selectableTable);
		this.setExpandRatio(selectableTable, 1);
		addRightClickSelect();
		completeInit();
	}

	/**
	 * overload this if you want to prevent the initial table fill
	 */
	protected void completeInit()
	{
		triggerFilter();
	}

	/**
	 * Adds a listener to select the right clicked item in the table. This is
	 * needed by ContextMenus.
	 */
	private void addRightClickSelect()
	{
		selectableTable.addItemClickListener(new ItemClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			@SuppressWarnings("deprecation")
			public void itemClick(ItemClickEvent event)
			{
				if (event.getButton() == MouseButton.RIGHT)
				{
					selectableTable.setValue(event.getItemId());
				}
			}
		});
	}

	abstract public HeadingPropertySet getHeadingPropertySet();

	abstract public Filterable getContainer();

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

	protected CrudSecurityManager getSecurityManager()
	{
		return SecurityManagerFactoryProxy.getSecurityManager(this.getClass());
	}

	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn)
	{
		selectableTable.addGeneratedColumn(id, generatedColumn);
	}

	protected AbstractLayout buildSearchBar()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		searchField.setWidth(100, Unit.PERCENTAGE);
		searchBar = layout;

		HorizontalLayout basicSearchLayout = new HorizontalLayout();
		basicSearchLayout.setWidth(100, Unit.PERCENTAGE);
		layout.addComponent(basicSearchLayout);

		AbstractLayout advancedSearch = buildAdvancedSearch();
		if (advancedSearch != null)
		{
			basicSearchLayout.addComponent(advancedSearchCheckbox);
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
		Button clear = createClearButton();
		basicSearchLayout.addComponent(clear);
		basicSearchLayout.setComponentAlignment(clear, Alignment.MIDDLE_CENTER);

		basicSearchLayout.addComponent(searchField);
		basicSearchLayout.setExpandRatio(searchField, 1.0f);

		searchField.focus();

		return layout;
	}

	@SuppressWarnings("deprecation")
	public void disableSelectable()
	{
		selectableTable.disableSelectable();
	}

	/**
	 * Filtering
	 * 
	 * @return
	 */
	private Button createClearButton()
	{

		Button clear = new Button("X");
		// clear.setStyleName(ValoTheme.BUTTON_SMALL);
		clear.setImmediate(true);
		clear.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				searchField.setValue("");
				clearAdvancedFilters();
				completeInit();

			}

		});
		return clear;
	}

	private AbstractLayout buildAdvancedSearch()
	{
		advancedSearchLayout = getAdvancedSearchLayout();
		if (advancedSearchLayout != null)
		{
			advancedSearchCheckbox = new Button("Advanced");
			advancedSearchOn = false;

			advancedSearchCheckbox.setImmediate(true);
			advancedSearchCheckbox.addClickListener(new ClickListener()
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
						completeInit();
					}
					if (!advancedSearchOn)
					{
						advancedSearchCheckbox.removeStyleName(ValoTheme.BUTTON_FRIENDLY);
					}
					else
					{
						advancedSearchCheckbox.setStyleName(ValoTheme.BUTTON_FRIENDLY);
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

	/**
	 * call this method to cause filters to be applied
	 */
	public void triggerFilter()
	{
		triggerFilter(searchField.getValue());
	}

	protected void triggerFilter(String searchText)
	{
		// boolean advancedSearchActive = advancedSearchCheckbox != null &&
		// advancedSearchCheckbox.getValue();
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
	{ /* Reset the filter for the Entity Container. */
		resetFilters();
		container.addContainerFilter(filter);

	}

	public String getSearchFieldText()
	{
		return filterString;
	}

	/**
	 * create a filter for the text supplied, the text is as entered in the text
	 * search bar.
	 * 
	 * @param string
	 * @return
	 */
	abstract protected Filter getContainerFilter(String filterString, boolean advancedSearchActive);

	/**
	 * called when the advancedFilters layout should clear it's values
	 */
	protected void clearAdvancedFilters()
	{

	}

	public Collection<Long> getSelectedIds()
	{
		return selectableTable.getSelectedIds();
	}

	@SuppressWarnings("deprecation")
	public void addSelectionListener(SelectionListener listener)
	{
		selectableTable.addSelectionListener(listener);

	}

	public void addItemClickListener(ItemClickListener object)
	{
		selectableTable.addItemClickListener(object);

	}

	public void removeAllContainerFilters()
	{
		container.removeAllContainerFilters();

	}

	public void addContainerFilter(Filter filter)
	{
		container.addContainerFilter(filter);

	}

	public void setConverter(String propertyId, Converter<String, ?> converter)
	{
		selectableTable.setConverter(propertyId, converter);

	}

	@SuppressWarnings("deprecation")
	public void setSelected(Collection<Long> ids)
	{
		selectableTable.setSelectedValue(ids);

	}

	@SuppressWarnings("deprecation")
	public void setMultiSelect(boolean b)
	{
		selectableTable.setMultiSelect(true);

	}

	public void setDragMode(TableDragMode mode)
	{
		selectableTable.setDragMode(mode);
	}

	public void setDropHandler(DropHandler dropHandler)
	{
		selectableTable.setDropHandler(dropHandler);

	}

	@SuppressWarnings("deprecation")
	public void deselectAll()
	{
		selectableTable.deselectAll();

	}

	@SuppressWarnings("deprecation")
	public Object getSelectedItems()
	{
		return selectableTable.getSelectedItems();
	}

	public void setSearchFilterText(String text)
	{
		searchField.setValue(text);
		triggerFilter(text);
	}

	public SelectableEntityTable<E> getSelectableTable()
	{
		return new SelectableEntityTable<E>(container, getHeadingPropertySet(), uniqueId);
	}

	public AbstractLayout getSearchBar()
	{
		return searchBar;
	}

	public SelectableEntityTable<E> getTable()
	{
		return selectableTable;
	}

	@SuppressWarnings("deprecation")
	public void selectAll()
	{
		selectableTable.selectAll();
	}

	public void setColumnReorderingAllowed(boolean columnReorderingAllowed)
	{
		selectableTable.setColumnReorderingAllowed(true);
	}

	public boolean isColumnReorderingAllowed()
	{
		return selectableTable.isColumnReorderingAllowed();
	}

	public void refresh()
	{
		selectableTable.refreshRowCache();
	}
}
