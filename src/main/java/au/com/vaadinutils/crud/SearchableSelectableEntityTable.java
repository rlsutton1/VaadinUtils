package au.com.vaadinutils.crud;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.listener.ClickEventLogged;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public abstract class SearchableSelectableEntityTable<E> extends VerticalLayout
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger();

	protected TextField searchField = new TextField();
	private AbstractLayout advancedSearchLayout;
	private VerticalLayout searchBar;
	private CheckBox advancedSearchCheckbox;
	private SelectableEntityTable<E> selectableTable;

	public SearchableSelectableEntityTable(EntityContainer<E> entityContainer, HeadingPropertySet<E> headingPropertySet)
	{
		selectableTable = new SelectableEntityTable<E>(entityContainer, headingPropertySet);
		selectableTable.setWidth("100%");

		AbstractLayout searchBar = buildSearchBar();

		this.addComponent(searchBar);
		this.addComponent(selectableTable);
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
			selectableTable.resetFilters();
		else
			selectableTable.applyFilter(filter);

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

}
