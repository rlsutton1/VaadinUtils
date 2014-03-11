package au.com.vaadinutils.reportFilter;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class ReportParameterString extends ReportParameter<String>
{

	protected TextField field;

	public ReportParameterString(String caption, String parameterName)
	{
		super( parameterName);
		field = new TextField();
		field.setCaption(caption);
	}

	@Override
	public String getValue()
	{
		return field.getValue();
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
	public void setDefaultValue(String defaultValue)
	{
		field.setValue(defaultValue);
		
	}

	@Override
	public String getExpectedParameterClassName()
	{
		return String.class.getCanonicalName();
	}

}
