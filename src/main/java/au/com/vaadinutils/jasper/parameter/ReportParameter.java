package au.com.vaadinutils.jasper.parameter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import au.com.vaadinutils.jasper.filter.ValidateListener;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;

public abstract class ReportParameter<T>
{
	final Set<String> parameters = new HashSet<String>();
	final protected String label;
	protected ValidateListener validateListener;

	public ReportParameter(String label, String parameterName)
	{
		parameters.add(parameterName);
		this.label = label;
	}

	public ReportParameter(String label, String parameterNames[])
	{
		for (String param : parameterNames)
		{
			parameters.add(param);
		}
		this.label = label;
	}

	public String getUrlEncodedKeyAndParameter(String parameterName) throws UnsupportedEncodingException
	{
		return parameterName + "=" + URLEncoder.encode(getValue(parameterName).toString(), "UTF-8");
	}

	public abstract Object getValue(String parameterName);

	public abstract Component getComponent();

	public abstract boolean shouldExpand();

	public abstract void setDefaultValue(T defaultValue);

	public Set<String> getParameterNames()
	{
		return parameters;
	}

	public abstract String getExpectedParameterClassName();

	public String getLabel()
	{
		return label;
	}

	public boolean showFilter()
	{
		return true;
	}

	abstract public String getDisplayValue(String parameterName);

	abstract public boolean validate();

	protected boolean validateField(AbstractField<T> field)
	{
		boolean valid = false;
		try
		{
			
			field.setComponentError(null);
			if (validateListener != null)
			{
				validateListener.setComponentError(null);
			}
			field.validate();
			valid = true;
		}
		catch (final InvalidValueException e)
		{
			ErrorMessage componentError = new ErrorMessage()
			{

				private static final long serialVersionUID = -2976235476811651668L;

				@Override
				public String getFormattedHtmlMessage()
				{
					return e.getHtmlMessage();
				}

				@Override
				public ErrorLevel getErrorLevel()
				{
					return ErrorLevel.ERROR;
				}
			};
			field.setComponentError(componentError);
			if (validateListener != null)
			{
				validateListener.setComponentError(componentError);
			}

		}
		return valid;

	}

	public void addValidateListener(ValidateListener listener)
	{
		this.validateListener = listener;

	}

	public boolean displayInreport()
	{
		return true;
	}

	abstract public void setValueAsString(String value,String paramterName) throws ReadOnlyException, ConversionException, ParseException;

	abstract public boolean isDateField();

	abstract public DateParameterType getDateParameterType();

	public Date getStartDate()
	{
		throw new RuntimeException("Date Parameters must overide and implement this method: "
				+ this.getClass().getCanonicalName());
	}

	public Date getEndDate()
	{
		throw new RuntimeException("Date Parameters must overide and implement this method: "
				+ this.getClass().getCanonicalName());
	}

	public void setStartDate(Date date)
	{
		throw new RuntimeException("Date Parameters must overide and implement this method: "
				+ this.getClass().getCanonicalName());
		
	}

	public void setEndDate(Date date)
	{
		throw new RuntimeException("Date Parameters must overide and implement this method: "
				+ this.getClass().getCanonicalName());
		
	}

}
