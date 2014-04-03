package au.com.vaadinutils.jasper.parameter;

import java.util.Set;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.dao.EntityManagerProvider;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.addon.jpacontainer.fieldfactory.MultiSelectConverter;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ReportParameterTable<T> extends ReportParameter<String>
{

	private Table table;
	private Long defaultValue = null;
	JPAContainer<T> container = null;
	private boolean notEmpty = false;
	private VerticalLayout layout;
	private String caption;

	public ReportParameterTable(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField, boolean multiSelect)
	{
		super(parameterName);
		init(caption, tableClass, displayField, multiSelect);

	}

	public ReportParameterTable(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField, boolean multiSelect, long defaultValue)
	{
		super(parameterName);
		init(caption, tableClass, displayField, multiSelect);
		this.defaultValue = defaultValue;
	}

	private void init(String caption, Class<T> tableClass, final SingularAttribute<T, String> displayField,
			boolean multiSelect)
	{
		container = JPAContainerFactory.makeBatchable(tableClass, EntityManagerProvider.getEntityManager());
		container.sort(new Object[] { displayField.getName() }, new boolean[] { true });

		container.setQueryModifierDelegate(getQueryModifierDelegate());

		layout = new VerticalLayout();
		layout.setSizeFull();
		this.caption = caption;

		TextField searchText = new TextField();
		searchText.setWidth("100%");
		searchText.setImmediate(true);
		searchText.setHeight("20");
		TextChangeListener t;
		searchText.addTextChangeListener(new TextChangeListener()
		{

		
			private static final long serialVersionUID = 1315710313315289836L;

			@Override
			public void textChange(TextChangeEvent event)
			{
				String value = event.getText();
				container.removeAllContainerFilters();
				if (value.length() > 0)
				{
					container.addContainerFilter(new SimpleStringFilter(displayField.getName(), value, true, false));
				}

			}
		});

		table = new Table();

		table.setSizeFull();
		// table.setHeight("150");

		table.setContainerDataSource(container);

		table.setConverter(MultiSelectConverter.class);

		table.setVisibleColumns(displayField.getName());
		table.setSelectable(true);
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		table.setNewItemsAllowed(false);
		table.setNullSelectionAllowed(false);
		table.setMultiSelect(multiSelect);

		layout.addComponent(new Label(caption));
		layout.addComponent(searchText);
		layout.addComponent(table);
		layout.setExpandRatio(table, 1);
	}

	/**
	 * override this method when providing a QueryModifierDelegate to filter the
	 * rows visible in the table
	 * 
	 * @return
	 */
	protected QueryModifierDelegate getQueryModifierDelegate()
	{
		return null;
	}

	public void addSelectionListener(ValueChangeListener listener)
	{
		table.addValueChangeListener(listener);
	}

	@Override
	public String getValue()
	{

		if (table.isMultiSelect())
		{
			@SuppressWarnings("unchecked")
			Set<Long> ids = (Set<Long>) table.getValue();
			String selection = "";
			for (Long id : ids)
			{
				selection += "" + id + ",";
			}
			if (selection.length() > 1)
			{
				selection = selection.substring(0, selection.length() - 1);
			}
			// supply default if emtpy
			if (selection.length() == 0 && defaultValue != null)
			{
				selection = "" + defaultValue;
			}
			if (notEmpty && selection.length() == 0)
			{
				Notification.show("Please select at least one " + caption, Type.ERROR_MESSAGE);
				throw new RuntimeException(caption + " can not be empty");
			}
			return selection;
		}
		String v = "" + table.getValue();
		if (v.length() == 0 && defaultValue != null)
		{
			v = "" + defaultValue;
		}
		if (notEmpty && v.length() == 0)
		{
			Notification.show("Please select at least one " + caption, Type.ERROR_MESSAGE);
			throw new RuntimeException(caption + " can not be empty");
		}
		return v;

	}

	public ReportParameter<?> setNotEmpty()
	{
		notEmpty = true;
		return this;
	}

	@Override
	public Component getComponent()
	{
		return layout;
	}

	@Override
	public boolean shouldExpand()
	{
		return true;
	}

	@Override
	public void setDefaultValue(String defaultValue)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getExpectedParameterClassName()
	{
		return String.class.getCanonicalName();
	}

}
