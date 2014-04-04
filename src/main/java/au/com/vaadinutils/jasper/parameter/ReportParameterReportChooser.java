package au.com.vaadinutils.jasper.parameter;

import au.com.vaadinutils.crud.FormHelper;
import au.com.vaadinutils.jasper.ui.JasperReportDataProvider;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class ReportParameterReportChooser<T extends Enum<T> & ReportChooser> extends ReportParameter<Enum<T>> implements ReportChooser
{

	private ComboBox field;

	/**
	 * 
	 * @param caption
	 * @param defaultValue
	 * @param parameterName
	 * @param enumClass
	 */
	public ReportParameterReportChooser(String caption, T defaultValue,String parameterName, Class<T> enumClass)
	{
		super(parameterName);
		field = new ComboBox(caption);
		field.setContainerDataSource(FormHelper.createContainerFromEnumClass("value", enumClass));
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setValue(defaultValue);
	}

	@Override
	public String getValue()
	{
		return field.getValue().toString();
	}

	@Override
	public Component getComponent()
	{
		return field;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
	}

	@Override
	public void setDefaultValue(Enum<T> defaultValue)
	{
		field.setValue(defaultValue);
		
	}

	@Override
	public String getExpectedParameterClassName()
	{
		return String.class.getCanonicalName();
	}

	@Override
	public JasperReportProperties getReportProperties(JasperReportDataProvider dataProvider)
	{
		@SuppressWarnings("unchecked")
		T e = (T)field.getValue();
		return e.getReportProperties(dataProvider);
	}
	
	
}
