package au.com.vaadinutils.jasper.parameter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.fields.TableCheckBoxSingleSelect;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.addon.jpacontainer.fieldfactory.MultiSelectConverter;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ReportParameterTableSingleSelect<T extends CrudEntity> extends ReportParameter<String> implements ReportParameterSelectionListener<T>
{

	protected TableCheckBoxSingleSelect table;
	private Long defaultValue = null;
	JPAContainer<T> container = null;
	private VerticalLayout layout;
	private String caption;
	Logger logger = LogManager.getLogger();
	private SingularAttribute<T, String> displayField;

	public ReportParameterTableSingleSelect(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField)
	{
		super(caption, parameterName);
		init(caption, tableClass, displayField);
		setNotEmpty();

	}

	public ReportParameterTableSingleSelect(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField,  Long defaultValue)
	{
		super(caption, parameterName);
		init(caption, tableClass, displayField);
		this.defaultValue = defaultValue;
		setNotEmpty();
	}

	private void init(String caption, Class<T> tableClass, final SingularAttribute<T, String> displayField)
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
				container.removeAllContainerFilters();
				if (value.length() > 0)
				{
					container.addContainerFilter(new SimpleStringFilter(displayField.getName(), value, true, false));
				}

			}
		});

		table = new TableCheckBoxSingleSelect();

		table.setSizeFull();
		// table.setHeight("150");

		table.setContainerDataSource(container);

		table.setConverter(MultiSelectConverter.class);

		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		setVisibleColumns(displayField);
		table.setColumnWidth(displayField.getName(), 130);
		table.setColumnExpandRatio(displayField.getName(), 1);
		table.setNewItemsAllowed(false);
		table.setNullSelectionAllowed(false);

		
		// removed for concertina
		// layout.addComponent(new Label(caption));
		layout.addComponent(searchText);
		layout.addComponent(table);
		
		layout.setExpandRatio(table, 1);
		// layout.setComponentAlignment(selectAll, Alignment.BOTTOM_RIGHT);

	}

	protected void setVisibleColumns(final SingularAttribute<T, String> displayField)
	{
		table.setVisibleColumns(displayField.getName());
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
		JPAContainer<T> cont = container =JpaBaseDao.getGenericDao(tableClass).createVaadinContainer();
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

	public void addSelectionListener(ValueChangeListener listener)
	{
		table.addValueChangeListener(listener);
	}

	public void removeAllContainerFilters()
	{
		((Container.Filterable) table.getContainerDataSource()).removeAllContainerFilters();
	}

	public void addContainerFilter(Filter filter)
	{
		((Container.Filterable) table.getContainerDataSource()).addContainerFilter(filter);
	}

	@Override
	public String getValue(String parameterName)
	{

		try
		{
			@SuppressWarnings("unchecked")
			Set<Long> ids = (Set<Long>) table.getSelectedItems();
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
			return selection;

		}
		catch (Exception e)
		{
			logger.error("Exception while getting value(s) for " + parameterName);
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean validate()
	{
		return super.validateField((AbstractField) table);
	}

	public void allowEmpty()
	{
		table.removeAllValidators();
	}

	private ReportParameter<?> setNotEmpty()
	{
		Validator validator = new Validator()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 8942263638713110223L;

			@SuppressWarnings("unchecked")
			@Override
			public void validate(Object value) throws InvalidValueException
			{
				setValidateListenerComponentError(null);
				table.setComponentError(null);
				setValidateListenerComponentError(null);
				table.setComponentError(null);
				Collection<Long> ids = (Collection<Long>) table.getSelectedItems();
				if (ids.size()==0)
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
					setValidateListenerComponentError(error);
					throw new Validator.EmptyValueException("You must select at least one " + caption);
				}

			}

			private void setValidateListenerComponentError(ErrorMessage error)
			{
				if (validateListener != null)
				{
					validateListener.setComponentError(error);
				}
			}
		};
		table.addValidator(validator);
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

	@Override
	public String getDisplayValue(String parameterName)
	{
		try
		{

			@SuppressWarnings("unchecked")
			Set<Long> ids = (Set<Long>) table.getSelectedItems();
			String selection = "";
			int ctr = 0;
			for (Long id : ids)
			{
				ctr++;
				selection += "" + table.getItem(id).getItemProperty(displayField.getName()) + ",";
				if (ctr > 2)
					break;
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
				selection = "" + table.getItemCaption(defaultValue);
			}
			return selection;

		}
		catch (Exception e)
		{
			for (String param : parameters)
			{
				logger.error("Exception while getting value(s) for {}, thread{}" , param,Thread.currentThread().getId());
			}
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValueAsString(String value, String parameterName)
	{
		String[] values = value.split(",");
		List<Long> idList = new LinkedList<Long>();

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
			table.setSelectedValue(idList);
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

}
