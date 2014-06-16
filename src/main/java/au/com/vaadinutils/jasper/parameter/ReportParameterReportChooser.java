package au.com.vaadinutils.jasper.parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.FormHelper;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class ReportParameterReportChooser<T extends Enum<T> & ReportChooser> extends ReportParameter<Enum<T>> implements
		ReportChooser
{

	private ComboBox field;
	private Class<T> enumClass;
	Logger logger = LogManager.getLogger();

	/**
	 * 
	 * @param caption
	 * @param defaultValue
	 * @param parameterName
	 * @param enumClass
	 */
	public ReportParameterReportChooser(String caption, T defaultValue, String parameterName, Class<T> enumClass)
	{
		super(caption, parameterName);
		field = new ComboBox(caption);
		this.enumClass = enumClass;
		field.setContainerDataSource(FormHelper.createContainerFromEnumClass("value", enumClass));
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setValue(defaultValue);
	}

	@Override
	public String getValue(String parameterName)
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
	public JasperReportProperties getReportProperties(JasperReportProperties reportProperties)
	{
		@SuppressWarnings("unchecked")
		T e = (T) field.getValue();
		return e.getReportProperties(reportProperties);
	}

	@Override
	public String getDisplayValue(String parameterName)
	{
		return getValue(null);
	}

	@Override
	public boolean validate()
	{
		return true;
	}

	@Override
	public void setValueAsString(String value, String parameterName)
	{

		boolean set = false;
		try
		{
			field.setValue(Enum.valueOf(enumClass, value));
			set = true;
		}
		catch (IllegalArgumentException e)
		{
			// we may have a toString method on the enum which will mean the
			// value that arrives here is the user friendly string
			for (T enumValue : enumClass.getEnumConstants())
			{
				if (enumValue.toString().equalsIgnoreCase(value))
				{
					field.setValue(enumValue);
					set = true;
					break;
				}
			}
			if (set == false)
			{
				logger.error(e, e);
			}
		}

	}

	@Override
	public boolean isDateField()
	{
		return false;
	}

	@Override
	public DateParameterType getDateParameterType()
	{
		throw new RuntimeException("Not implemented");
	}

}
