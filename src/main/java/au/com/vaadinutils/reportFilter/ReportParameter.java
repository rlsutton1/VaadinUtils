package au.com.vaadinutils.reportFilter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.vaadin.ui.Component;

public abstract class ReportParameter<T>
{
	final String parameterName;

	public ReportParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public String getUrlEncodedKeyAndParameter() throws UnsupportedEncodingException
	{
		return parameterName + "=" + URLEncoder.encode(getValue().toString(), "UTF-8");
	}

	public abstract T getValue();

	public abstract Component getComponent();

	public abstract boolean shouldExpand();

	public abstract void setDefaultValue(T defaultValue);
}
