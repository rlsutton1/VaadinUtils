package au.com.vaadinutils.jasper.parameter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;

public class ReportParameterDateTime extends ReportParameter<String>
{
	protected DateField field;

	protected String parameterFormat = "yyyy/MM/dd HH:mm:ss";

	/**
	 * 
	 * @param caption
	 *            - shown on the UI
	 * @param parameterName
	 *            - parameter name passed to ireport
	 * @param resolution
	 *            - Vaadin calendar control resolution
	 * @param displayFormat
	 *            - format to display to the user
	 * @param parameterFormat
	 *            - format of the value passed to ireport
	 */
	public ReportParameterDateTime(String caption, String parameterName, Resolution resolution, String displayFormat,
			String parameterFormat)
	{
		super(caption, parameterName);
		field = new DateField(caption, new DateTime().toDate());
		field.setResolution(resolution);
		field.setDateFormat(displayFormat);
		this.parameterFormat = parameterFormat;
		field.setImmediate(true);

		field.setValidationVisible(true);
	}
	
	public void addValueChangeListener(ValueChangeListener listener)
	{
		field.addValueChangeListener(listener);
		
	}

	public ReportParameterDateTime(String caption, String parameterName)
	{
		super(caption, parameterName);
		field = new DateField(caption, new DateTime().withTimeAtStartOfDay().toDate());
		field.setResolution(Resolution.DAY);
		field.setDateFormat("yyyy/MM/dd");

		field.setValidationVisible(true);

	}

	@Override
	public String getValue()
	{
		SimpleDateFormat sdf = new SimpleDateFormat(parameterFormat);
		return sdf.format(field.getValue());
	}

	public Date getDate()
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
		// this.field.setValue(defaultValue);

	}

	@Override
	public String getExpectedParameterClassName()
	{
		return null;
	}

	@Override
	public String getDisplayValue()
	{
		return new SimpleDateFormat(field.getDateFormat()).format(field.getValue());
	}

	@Override
	public boolean validate()
	{
		return true;
	}
	
	
}
