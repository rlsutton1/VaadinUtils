package au.com.vaadinutils.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.EntityTable;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.SearchableSelectableEntityTable;
import au.com.vaadinutils.dao.JpaBaseDao;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Buffered;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class TwinColumnSearchableSelect<P extends CrudEntity, C extends ChildCrudEntity> extends
	CustomField<Collection<C>>
{

    private static final long serialVersionUID = -4316521010865902678L;
    private SetAttribute<P, C> relation;
    private SingularAttribute<C, ?> listField;

    private Collection<C> sourceValue;
    private Table selectedCols;
    Logger logger = LogManager.getLogger();
    private BeanContainer<Long, C> beans;
    private JPAContainer<C> availableContainer;
    private SearchableSelectableEntityTable<C> available;

    /**
     * Unfortunately TwinColumnSelect wont work with large sets, it isn't
     * searchable and it doesn't lazy load, it also isn't sortable.
     * 
     * Hopefully I'll address all of these issues here.
     */

    public TwinColumnSearchableSelect(SetAttribute<P, C> relation, SingularAttribute<C, ?> listField)
    {
	this.relation = relation;
	this.listField = listField;
    }

    @Override
    protected Component initContent()
    {
	HorizontalLayout mainLayout = new HorizontalLayout();
	mainLayout.setSizeFull();
	mainLayout.setSpacing(true);
	selectedCols = new Table();
	selectedCols.setContainerDataSource(createBeanContainer());
	selectedCols.setVisibleColumns(listField.getName());
	selectedCols.setColumnHeaders("Selected");
	selectedCols.setSizeFull();
	selectedCols.setHeight("200");
	selectedCols.setSelectable(true);

	mainLayout.addComponent(selectedCols);

	createAvailableTable();

	mainLayout.addComponent(buildButtons());

	
	mainLayout.addComponent(available);

	return mainLayout;
    }

    private void createAvailableTable()
    {
	availableContainer = JpaBaseDao.getGenericDao(relation.getElementType().getJavaType()).createVaadinContainer();

	 available = new SearchableSelectableEntityTable<C>()
	{

	    @Override
	    public HeadingPropertySet<C> getHeadingPropertySet()
	    {
		return new HeadingPropertySet.Builder<C>().addColumn("Available", listField).build();
	    }

	    @Override
	    public Filterable getContainer()
	    {
		return availableContainer;
	    }

	    @Override
	    protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
	    {
		Filter filter = null;
		if (filterString != null && filterString.length() > 0)
		{
		    filter = new SimpleStringFilter(listField.getName(), filterString, true, false);
		}
		return filter;
	    }
	    @Override
	    protected String getTitle()
	    {
		return "";
	    }
	};
	available.disableSelectable();
	// should be a searchableEntityTable

	available.setSizeFull();
	available.setHeight("200");
    }

    private Component buildButtons()
    {
	VerticalLayout layout = new VerticalLayout();
	layout.setSizeFull();
	layout.setWidth("50");
	layout.setHeight("100");
	Button remove = new Button(">>");
	remove.addClickListener(new ClickListener()
	{

	    @Override
	    public void buttonClick(ClickEvent event)
	    {
		try
		{
		    Long id = (Long) selectedCols.getValue();
		    System.out.println(id);
		    beans.removeItem(id);
		    System.out.println(beans.getItemIds());
		}
		catch (Exception e)
		{
		    logger.error(e, e);
		}

	    }
	});
	remove.setHeight("50");
	Button add = new Button("<<");
	add.addClickListener(new ClickListener()
	{

	    @SuppressWarnings("unchecked")
	    @Override
	    public void buttonClick(ClickEvent event)
	    {
		List<Long> ids = new LinkedList<>();
		ids.addAll((Collection<? extends Long>) available.getSelectedItems());
		Long id = ids.get(0);
		if (id != null)
		{
		    JpaBaseDao<C, Long> dao = (JpaBaseDao<C, Long>) JpaBaseDao.getGenericDao(relation.getElementType()
			    .getJavaType());
		    C cust = dao.findById(id);
		    if (cust != null)
		    {
			beans.addBean(cust);
		    }
		}

	    }
	});
	layout.addComponent(remove);
	layout.addComponent(add);

	return layout;

    }

    private BeanContainer<Long, C> createBeanContainer()
    {
	beans = new BeanContainer<Long, C>(listField.getDeclaringType().getJavaType());

	// Use the name property as the item ID of the bean
	// listField.getDeclaringType().
	beans.setBeanIdProperty("customerID");
	return beans;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void commit() throws Buffered.SourceException, InvalidValueException
    {
	super.commit();
	Collection<C> tmp = (Collection<C>) getConvertedValue();
	if (sourceValue == null)
	{
	    sourceValue = tmp;
	}
	for (C c : tmp)
	{
	    if (!sourceValue.contains(c))
	    {
		sourceValue.add(c);
	    }
	}
	Set<C> toRemove = new HashSet<>();
	for (C c : sourceValue)
	{
	    if (!tmp.contains(c))
	    {
		toRemove.add(c);
	    }
	}
	sourceValue.removeAll(toRemove);

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isModified()
    {
	Collection<C> convertedValue = (Collection<C>) getConvertedValue();
	if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue != null && convertedValue.size() > 0))
	    return true;
	if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue == null || convertedValue.size() == 0))
	    return false;
	boolean equal = convertedValue.containsAll(sourceValue) && sourceValue.containsAll(convertedValue);
	return !equal;
    }

    @Override
    public void setReadOnly(boolean b)
    {
	selectedCols.setReadOnly(b);
	super.setReadOnly(b);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setInternalValue(Collection<C> newValue)
    {
	super.setInternalValue(newValue);

	// render text field
	selectedCols.setReadOnly(false);
	beans.removeAllItems();
	beans.addAll(newValue);
	selectedCols.setReadOnly(super.isReadOnly());

	sourceValue = (Collection<C>) getConvertedValue();
    }

    public Object getConvertedValue()
    {

	Collection<C> selected = new HashSet<>();
	if (beans != null)
	{
	    for (Long id : beans.getItemIds())
	    {
		selected.add(beans.getItem(id).getBean());
	    }
	}
	return selected;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Collection<C>> getType()
    {
	return (Class<? extends Collection<C>>) Collection.class;
    }

}
