package au.com.vaadinutils.search;

import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;

public class SearchableSelect<T> extends MVerticalLayout
{
	private static final long serialVersionUID = 1L;
	private MTable<T> selector;
	MTextField searchField;
	private SelectProvider<T> provider;
	private MHorizontalLayout searchPanel;

	public SearchableSelect(String caption, SelectProvider<T> provider)
	{
		this.provider = provider;
		selector = buildSelector(provider);
		selector.lazyLoadFrom(provider, provider);
		selector.select(selector.getCurrentPageFirstItemId());
		selector.setHeight("400");
		selector.setWidth("300");

		buildSearchPanel();
		add(new Label(caption));
		add(searchPanel);
		add(selector);

		setMargin(false);

	}

	@SuppressWarnings("deprecation")
	private MTable<T> buildSelector(SelectProvider<T> provider)
	{
		MTable<T> sel = new MTable<>(provider.getType()).withProperties(provider.getProperties())
				.withColumnHeaders(provider.getHeaders());
		sel.lazyLoadFrom(provider, provider);

		sel.setImmediate(true);

		sel.setSelectable(true);
		sel.setMultiSelect(false);
		sel.setMultiSelectMode(MultiSelectMode.SIMPLE);
		sel.setNullSelectionAllowed(false);

		sel.getColumnHeaders();

		sel.setSizeFull();

		return sel;
	}

	private void buildSearchPanel()
	{

		searchField = new MTextField().withFullWidth();
		searchField.addTextChangeListener(new TextChangeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void textChange(final TextChangeEvent event)
			{
				provider.setFilterText(event.getText());
				selector.lazyLoadFrom(provider, provider);
			}
		});

		final MButton clearButton = new MButton("X").withListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event)
			{
				searchField.setValue("");
				provider.setFilterText("");

				selector.lazyLoadFrom(provider, provider);

			}
		});

		searchPanel = new MHorizontalLayout().add(clearButton, searchField).expand(searchField);

	}

	public T getSelection()
	{
		return selector.getValue();
	}

	public void select(T value)
	{
		selector.setValue(value);
	}
}
