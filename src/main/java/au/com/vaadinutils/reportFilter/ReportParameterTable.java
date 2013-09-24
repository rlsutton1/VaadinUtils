package au.com.vaadinutils.reportFilter;

import java.util.Set;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.dao.EntityManagerProvider;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.MultiSelectConverter;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.ListSelect;

public class ReportParameterTable<T> extends ReportParameter
{

	private ListSelect field;

	public ReportParameterTable(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField, boolean multiSelect)
	{
		super(parameterName);
		field = new ListSelect(caption);
		field.setSizeFull();

		field.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		field.setItemCaptionPropertyId(displayField.getName());

		JPAContainer<T> queueContainer = JPAContainerFactory.makeBatchable(tableClass,
				EntityManagerProvider.getEntityManager());
		field.setContainerDataSource(queueContainer);
		queueContainer.sort(new Object[] { displayField.getName() }, new boolean[] { true });
		
		field.setConverter(MultiSelectConverter.class);

		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setMultiSelect(multiSelect);

	}

	@Override
	protected String getValue()
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
			return selection;
		}

		return "" + field.getValue();

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

}
