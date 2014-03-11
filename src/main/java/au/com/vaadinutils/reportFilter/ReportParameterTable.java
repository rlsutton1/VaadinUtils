package au.com.vaadinutils.reportFilter;

import java.util.Set;

import javax.persistence.metamodel.SingularAttribute;

import org.tepi.filtertable.FilterTable;

import au.com.vaadinutils.dao.EntityManagerProvider;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.MultiSelectConverter;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Component;

public class ReportParameterTable<T> extends ReportParameter<String>
{

	private FilterTable field;
	private Long defaultValue = null;
	JPAContainer<T> container = null;
	
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

	private void init(String caption, Class<T> tableClass, SingularAttribute<T, String> displayField,
			boolean multiSelect)
	{
		field = new FilterTable(caption);
		field.setSizeFull();

		field.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		field.setItemCaptionPropertyId(displayField.getName());

		container = JPAContainerFactory.makeBatchable(tableClass,
				EntityManagerProvider.getEntityManager());
		field.setContainerDataSource(container);
		container.sort(new Object[] { displayField.getName() }, new boolean[] { true });

		field.setConverter(MultiSelectConverter.class);

		field.setVisibleColumns(displayField.getName());
		field.setSelectable(true);
		field.setColumnHeaders(displayField.getName());
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setMultiSelect(multiSelect);
		field.setFilterBarVisible(true);
	}
	
	public void addSelectionListener(ValueChangeListener listener)
	{
		field.addValueChangeListener(listener);
	}
	

	@Override
	public String getValue()
	{

		if (field.isMultiSelect())
		{
			@SuppressWarnings("unchecked")
			Set<Long> ids = (Set<Long>) field.getValue();
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
			if (selection.length()==0&& defaultValue != null)
			{
				selection = "" + defaultValue;
			}
			return selection;
		}
		String v = "" + field.getValue();
		if (v.length() == 0 && defaultValue != null)
		{
			v = "" + defaultValue;
		}
		return v;

	}

	@Override
	public Component getComponent()
	{
		return field;
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

}
