package au.com.vaadinutils.jasper.parameter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.GridHeadingPropertySet;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

public class ReportParameterTable<T extends CrudEntity> extends ReportParameter<String>
		implements ReportParameterSelectionListener<T>
{

	protected Grid grid;
	private Long defaultValue = null;
	JPAContainer<T> container = null;
	protected VerticalLayout layout;
	protected String caption;
	Logger logger = LogManager.getLogger();
	protected SingularAttribute<T, String> displayField;

	public ReportParameterTable(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField)
	{
		super(caption, parameterName);
		init(caption, tableClass, displayField);
		setSelectionMode(SelectionMode.MULTI);
	}

	public ReportParameterTable(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField, Long defaultValue)
	{
		super(caption, parameterName);
		init(caption, tableClass, displayField);
		this.defaultValue = defaultValue;
		setSelectionMode(SelectionMode.MULTI);

	}

	protected void addComponentToLayout(Component comp)
	{
		layout.addComponent(comp);
	}

	void setSelectionMode(SelectionMode mode)
	{
		if (mode == SelectionMode.MULTI)
		{
			grid.setSelectionMode(SelectionMode.MULTI);

			grid.setSelectionModel(new Grid.MultiSelectionModel());
		}
		else if (mode == SelectionMode.SINGLE)
		{
			grid.setSelectionMode(SelectionMode.SINGLE);

			grid.setSelectionModel(new Grid.SingleSelectionModel());

		}
		else
		{
			throw new RuntimeException("SelectionMode none not supported");
		}

	}

	protected void init(String caption, Class<T> tableClass, final SingularAttribute<T, String> displayField)
	{
		JpaBaseDao.getGenericDao(tableClass).flushCache();
		container = createContainer(tableClass, displayField);
		this.displayField = displayField;
		layout = new VerticalLayout();
		layout.setSizeFull();
		this.caption = caption;

		TextField searchText = new TextField();
		searchText.setInputPrompt("Search");
		searchText.setWidth("100%");
		searchText.setImmediate(true);
		searchText.setHeight("20");
		searchText.addTextChangeListener(new TextChangeListener()
		{

			private static final long serialVersionUID = 1315710313315289836L;

			@Override
			public void textChange(TextChangeEvent event)
			{
				String value = event.getText();
				removeAllContainerFilters();
				if (value.length() > 0)
				{
					container.addContainerFilter(new SimpleStringFilter(displayField.getName(), value, true, false));
				}

			}
		});

		grid = new Grid();
		grid.setImmediate(true);

		grid.setSizeFull();
		// table.setHeight("150");

		grid.setContainerDataSource(container);

		new GridHeadingPropertySet.Builder<T>().createColumn(caption, displayField.getName()).build().applyToGrid(grid);

		List<SortOrder> orders = new LinkedList<>();
		orders.add(new SortOrder(displayField.getName(), SortDirection.ASCENDING));
		grid.setSortOrder(orders);

		final Label selectionCount = new Label("0 selected");

		// removed for concertina
		// layout.addComponent(new Label(caption));
		layout.addComponent(searchText);
		layout.addComponent(grid);

		HorizontalLayout selectionLayout = new HorizontalLayout();
		selectionLayout.setHeight("30");
		selectionLayout.setWidth("100%");
		selectionLayout.addComponent(selectionCount);
		selectionLayout.setComponentAlignment(selectionCount, Alignment.MIDDLE_RIGHT);
		layout.addComponent(selectionLayout);
		grid.addSelectionListener(new SelectionListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void select(SelectionEvent event)
			{
				validate();

				selectionCount.setValue("" + event.getSelected().size() + " selected");

			}
		});

		layout.setExpandRatio(grid, 1);
		// layout.setComponentAlignment(selectAll, Alignment.BOTTOM_RIGHT);

	}

	/**
	 * overload this method to create something more than just a single entity.
	 * 
	 * @param tableClass
	 * @param displayField
	 * @return
	 */
	protected JPAContainer<T> createContainer(Class<T> tableClass, final SingularAttribute<T, String> displayField)
	{
		JPAContainer<T> cont = container = JpaBaseDao.getGenericDao(tableClass).createVaadinContainer();
		cont.sort(new Object[] { displayField.getName() }, new boolean[] { true });

		cont.setQueryModifierDelegate(getQueryModifierDelegate());
		return cont;
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

	@Override
	public void addSelectionListener(final ValueChangeListener listener)
	{
		grid.addSelectionListener(new SelectionListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void select(SelectionEvent event)
			{

				listener.valueChange(new ValueChangeEvent()
				{

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public Property<Collection<Long>> getProperty()
					{
						return new Property<Collection<Long>>()
						{

							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							public Collection<Long> getValue()
							{
								return getSelectedIds();
							}

							@Override
							public void setValue(Collection<Long> newValue)
									throws com.vaadin.data.Property.ReadOnlyException
							{

							}

							@Override
							public Class<? extends Collection<Long>> getType()
							{
								return null;
							}

							@Override
							public boolean isReadOnly()
							{
								return false;
							}

							@Override
							public void setReadOnly(boolean newStatus)
							{

							}
						};
					}
				});

			}
		});

	}

	private Collection<Long> getSelectedIds()
	{
		Collection<Long> ids = new LinkedList<>();
		for (Object id : grid.getSelectedRows())
		{
			ids.add((Long) id);
		}
		return ids;
	}

	public void removeAllContainerFilters()
	{
		((Container.Filterable) grid.getContainerDataSource()).removeAllContainerFilters();
	}

	public void addContainerFilter(Filter filter)
	{
		((Container.Filterable) grid.getContainerDataSource()).addContainerFilter(filter);
	}

	@Override
	public String getValue(String parameterName)
	{

		try
		{
			Collection<Object> ids = grid.getSelectedRows();
			String selection = "";
			for (Object id : ids)
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
			return selection;

		}
		catch (Exception e)
		{
			logger.error("Exception while getting value(s) for " + parameterName);
			throw new RuntimeException(e);
		}

	}

	@Override
	public boolean validate()
	{
		validateMe();
		return getValue().size() > 0;
	}

	public void validateMe()
	{

		grid.setComponentError(null);
		setComponentErrorForValidateListener(null);
		grid.setComponentError(null);
		setComponentErrorForValidateListener(null);
		Collection<Long> ids = getSelectedIds();
		if (ids.size() == 0)
		{
			ErrorMessage error = new ErrorMessage()
			{

				private static final long serialVersionUID = -6437991860908562482L;

				@Override
				public ErrorLevel getErrorLevel()
				{
					return ErrorLevel.ERROR;
				}

				@Override
				public String getFormattedHtmlMessage()
				{
					return "You must select at least one " + caption;
				}
			};
			grid.setComponentError(error);
			setComponentErrorForValidateListener(error);

		}

	}

	private void setComponentErrorForValidateListener(ErrorMessage error)
	{
		if (validateListener != null)
		{
			validateListener.setComponentError(error);
		}
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

	@Override
	public String getDisplayValue(String parameterName)
	{
		try
		{

			Collection<Object> ids = grid.getSelectedRows();
			String selection = "";
			int ctr = 0;
			for (Object id : ids)
			{
				ctr++;
				final Item item = grid.getContainerDataSource().getItem(id);

				// we get nulls if the entity is deleted from the database after
				// the report parameter is saved
				if (item != null)
				{
					selection += "" + item.getItemProperty(displayField.getName()) + ",";
				}
				if (ctr > 2)
				{
					break;
				}
			}
			if (selection.length() > 1)
			{
				selection = selection.substring(0, selection.length() - 1);
			}
			if (ctr != ids.size())
			{
				selection += " (+" + (ids.size() - ctr) + " more)";
			}
			// supply default if emtpy
			if (selection.length() == 0 && defaultValue != null)
			{
				selection = "" + defaultValue;
			}
			return selection;

		}
		catch (Exception e)
		{
			for (String param : parameters)
			{
				logger.error("Exception while getting value(s) for {}, thread{}", param,
						Thread.currentThread().getId());
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValueAsString(String value, String parameterName)
	{
		String[] values = value.split(",");
		List<Long> idList = new LinkedList<>();

		for (String id : values)
		{
			if (id.length() > 0)
			{
				Long intId = Long.parseLong(id);
				if (intId != -1)
				{
					idList.add(intId);
				}
			}
		}
		if (idList.size() > 0)
		{
			for (Long id : idList)
			{
				try
				{
					grid.select(id);
				}
				catch (IllegalArgumentException e)
				{
					logger.warn("Id doesn't exist in container");
				}
			}

		}

	}

	@Override
	public boolean isDateField()
	{
		return false;
	}

	@Override
	public DateParameterType getDateParameterType()
	{
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Collection<Object> getValue()
	{

		return grid.getSelectedRows();
	}

}
