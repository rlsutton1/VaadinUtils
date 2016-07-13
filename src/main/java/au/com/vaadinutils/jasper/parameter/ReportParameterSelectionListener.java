package au.com.vaadinutils.jasper.parameter;

import java.util.Collection;

import com.vaadin.data.Property.ValueChangeListener;

import au.com.vaadinutils.crud.CrudEntity;

public interface ReportParameterSelectionListener<E extends CrudEntity>
{

	void addSelectionListener(ValueChangeListener listener);

	Collection<Object> getValue();

}
