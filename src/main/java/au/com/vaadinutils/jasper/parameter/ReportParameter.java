package au.com.vaadinutils.jasper.parameter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import au.com.vaadinutils.jasper.filter.ValidateListener;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;

public abstract class ReportParameter<T>
{
	final String parameterName;
	final protected String label;
	protected ValidateListener validateListener;

	public ReportParameter(String label, String parameterName)
	{
		this.parameterName = parameterName;
		this.label = label;
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

	public String getLabel()
	{
		return label;
	}

	public boolean showFilter()
	{
		return true;
	}

	abstract public String getDisplayValue();

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

}
