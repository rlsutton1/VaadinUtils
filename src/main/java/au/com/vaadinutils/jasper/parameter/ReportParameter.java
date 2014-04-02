package au.com.vaadinutils.jasper.parameter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.vaadin.ui.Component;

public abstract class ReportParameter<T>
{
	String parameterName;
	String label;

	public ReportParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public String getUrlEncodedKeyAndParameter() throws UnsupportedEncodingException
	{
		return parameterName + "=" + URLEncoder.encode(getValue().toString(), "UTF-8");
	}

	public abstract Object getValue();

	public abstract Component getComponent();

	public abstract boolean shouldExpand();

	public abstract void setDefaultValue(T defaultValue);

	public String getParameterName()
	{
		return parameterName;
	}

	public abstract String getExpectedParameterClassName();

	public void setLabel(String label)
	{
		this.label = label;

	}

	public boolean showFilter()
	{
		return true;
	}

	public void setParameterName(String newName)
	{
		parameterName = newName;

	}

}
