package au.com.vaadinutils.jasper.parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class ReportParameterString extends ReportParameter<String>
{
	Logger logger = LogManager.getLogger();

	protected TextField field;

	public ReportParameterString(String caption, String parameterName)
	{
		super( caption,parameterName);
		field = new TextField();
		field.setCaption(caption);
	}

	@Override
	public String getValue(String parameterName)
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

	@Override
	public String getDisplayValue(String parameterName)
	{
		return getValue(null);
	}

	@Override
	public boolean validate()
	{
		return super.validateField(field);
	}
	
	public ReportParameter<?> setNotEmpty()
	{
		Validator validator = new Validator(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 8942263638713110223L;

			@Override
			public void validate(Object value) throws InvalidValueException
			{
				logger.info(value);
				
			}};
			field.addValidator(validator);
		return this;
	}

	@Override
	public void setValueAsString(String value, String parameterName)
	{
		field.setValue(value);
		
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
