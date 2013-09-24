package au.com.vaadinutils.reportFilter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.vaadin.ui.Component;

public abstract class ReportParameter
{
	final String parameterName;

	public ReportParameter(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public String getUrlEncodedKeyAndParameter() throws UnsupportedEncodingException
	{
		return parameterName + "=" + URLEncoder.encode(getValue(), "UTF-8");
	}

	abstract protected String getValue();

	public abstract Component getComponent();

	public abstract boolean shouldExpand();

}
