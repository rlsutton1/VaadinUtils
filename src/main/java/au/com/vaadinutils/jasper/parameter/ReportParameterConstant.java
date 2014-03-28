package au.com.vaadinutils.jasper.parameter;

import com.vaadin.ui.Component;

public class ReportParameterConstant extends ReportParameter<String>
{

	private String value;

	public ReportParameterConstant(String parameterName, String value)
	{
		super(parameterName);
		this.value = value;

	}

	@Override
	public String getValue()
	{

		return value;
	}

	@Override
	public Component getComponent()
	{
		return null;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
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

	public boolean showFilter()
	{
		return false;
	}

}
