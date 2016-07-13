package au.com.vaadinutils.jasper.parameter;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.ui.Grid.SelectionMode;

import au.com.vaadinutils.crud.CrudEntity;

public class ReportParameterTableSingleSelect<T extends CrudEntity> extends ReportParameterTable<T>
{

	public ReportParameterTableSingleSelect(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField)
	{
		super(caption, parameterName, tableClass, displayField);
		setSelectionMode(SelectionMode.SINGLE);
	}

	public ReportParameterTableSingleSelect(String caption, String parameterName, Class<T> tableClass,
			SingularAttribute<T, String> displayField, Long defaultValue)
	{
		super(caption, parameterName, tableClass, displayField, defaultValue);
		setSelectionMode(SelectionMode.SINGLE);
	}

}
