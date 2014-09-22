package au.com.vaadinutils.crud;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.security.SecurityManagerFactoryProxy;
import au.com.vaadinutils.fields.SelectionListener;
import au.com.vaadinutils.listener.ClickEventLogged;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public abstract class SearchableSelectableEntityTable<E> extends VerticalLayout
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger();

	protected TextField searchField = new TextField();
	private AbstractLayout advancedSearchLayout;
	private VerticalLayout searchBar;
	private CheckBox advancedSearchCheckbox;
	protected SelectableEntityTable<E> selectableTable;
	protected Container.Filterable container;

	public SearchableSelectableEntityTable()
	{
		container = getContainer();
		selectableTable = new SelectableEntityTable<E>(container, getHeadingPropertySet());
		selectableTable.setSizeFull();

		if (!getSecurityManager().canUserView())
		{
			this.setSizeFull();
			this.addComponent(new Label("Sorry, you do not have permission to access " + getTitle()));
			return;
		}

		AbstractLayout searchBar = buildSearchBar();
		
		Label title = new Label(getTitle());
		title.setStyleName(Reindeer.LABEL_H1);
		this.addComponent(title);
		this.addComponent(searchBar);
		this.addComponent(selectableTable);
		this.setExpandRatio(selectableTable, 1);
		triggerFilter();
	}

	abstract public HeadingPropertySet<E> getHeadingPropertySet();

	abstract public Filterable getContainer();

	abstract public String getTitle();

	private CrudSecurityManager getSecurityManager()
	{
		return SecurityManagerFactoryProxy.getSecurityManager(this.getClass());
	}

	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn)
	{
		selectableTable.addGeneratedColumn(id, generatedColumn);
	}

	private AbstractLayout buildSearchBar()
	{
		searchBar = new VerticalLayout();
		searchBar.setWidth("100%");
		searchField.setWidth("100%");

		HorizontalLayout basicSearchLayout = new HorizontalLayout();
		basicSearchLayout.setSizeFull();
		basicSearchLayout.setSpacing(true);
		searchBar.addComponent(basicSearchLayout);

		AbstractLayout advancedSearch = buildAdvancedSearch();
		if (advancedSearch != null)
		{
			basicSearchLayout.addComponent(advancedSearchCheckbox);
		}

		searchField.setInputPrompt("Search");
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		searchField.setImmediate(true);
		searchField.addTextChangeListener(new TextChangeListener()
		{
			private static final long serialVersionUID = 1L;

			public void textChange(final TextChangeEvent event)
			{
				String filterString = event.getText().trim();
				triggerFilter(filterString);
			}

		});

		basicSearchLayout.addComponent(searchField);
		basicSearchLayout.setExpandRatio(searchField, 1.0f);
		basicSearchLayout.setSpacing(true);

		// clear button
		Button clear = createClearButton();
		basicSearchLayout.addComponent(clear);
		basicSearchLayout.setComponentAlignment(clear, Alignment.MIDDLE_CENTER);

		searchField.focus();

		return searchBar;
	}

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
			advancedSearchCheckbox = new CheckBox("Advanced");

			advancedSearchCheckbox.setImmediate(true);
			advancedSearchCheckbox.addValueChangeListener(new ValueChangeListener()
			{

				/**
				 * 
				 */
				private static final long serialVersionUID = -4396098902592906470L;

				@Override
				public void valueChange(ValueChangeEvent arg0)
				{
					advancedSearchLayout.setVisible(advancedSearchCheckbox.getValue());
					if (!advancedSearchCheckbox.getValue())
					{
						triggerFilter();
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
	protected void triggerFilter()
	{
		triggerFilter(searchField.getValue());
	}

	private void triggerFilter(String searchText)
	{
		boolean advancedSearchActive = advancedSearchCheckbox != null && advancedSearchCheckbox.getValue();
		Filter filter = getContainerFilter(searchText, advancedSearchActive);
		if (filter == null)
			resetFilters();
		else
			applyFilter(filter);

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

	protected String getSearchFieldText()
	{
		return searchField.getValue();
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

	public void setConverter(String propertyId,
			Converter<String, ?> converter)
	{
		selectableTable.setConverter(propertyId,converter);
		
	}

}
