package au.com.vaadinutils.jasper.parameter;

import java.util.Collection;
import java.util.Set;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Or;

abstract public class ReportParameterDependantTableSingleSelect< P extends CrudEntity, T extends CrudEntity> extends
		ReportParameterTableSingleSelect<T>
{
	/**
	 * extend this type when you need to do dependant selects between
	 * ReportParameterTable types.
	 * 
	 * @param caption
	 * @param parameterName
	 * @param tableClass
	 * @param displayField
	 * @param multiSelect
	 */
	public ReportParameterDependantTableSingleSelect(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField)
	{
		super(caption, parameterName, tableClass, displayField,  null);

	}

	public abstract String getPrimaryKeyFieldName();

	/**
	 * provide the parent ReportParameterTable, this method will attach this
	 * parameter as a value change listener so that selections will cascade to
	 * the reportparameter
	 * 
	 * @param parent
	 */
	public void setParentFilter(ReportParameterSelectionListener<P> parent)
	{
		ValueChangeListener listener = new ValueChangeListener()
		{

			private static final long serialVersionUID = -3033912391060894738L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				@SuppressWarnings("unchecked")
				Collection<Long> selectedIds = (Collection<Long>) event.getProperty().getValue();
				removeAllContainerFilters();
				boolean filtersAdded = false;
				if (selectedIds.size() > 0)
				{
					Filter filter = null;
					for (Long parentId : selectedIds)
					{
						for (Long id : mapParentIdToPrimaryKey(parentId))
						{
							filtersAdded = true;
							if (filter == null)
							{
								filter = new Compare.Equal(getPrimaryKeyFieldName(), id);
							}
							else
							{
								filter = new Or(filter, new Compare.Equal(getPrimaryKeyFieldName(), id));
							}
						}
					}
					if (filter != null)
					{
						addContainerFilter(filter);
					}
				}
				if (!filtersAdded)
				{
					addContainerFilter(new Compare.Equal(getPrimaryKeyFieldName(), -1));
				}
				table.deselectAll();

			}
		};
		addContainerFilter(new Compare.Equal(getPrimaryKeyFieldName(), -1));

		parent.addSelectionListener(listener);

	}

	abstract public Set<Long> mapParentIdToPrimaryKey(Long parentId);

}
