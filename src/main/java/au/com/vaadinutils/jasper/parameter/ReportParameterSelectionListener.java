package au.com.vaadinutils.jasper.parameter;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.data.Property.ValueChangeListener;

public interface ReportParameterSelectionListener<E extends CrudEntity>
{

	void addSelectionListener(ValueChangeListener listener);

}
