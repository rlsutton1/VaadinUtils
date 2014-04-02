package au.com.vaadinutils.jasper.parameter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

import com.vaadin.shared.ui.datefield.Resolution;
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
		super(parameterName);
		field = new DateField(caption, new DateTime().toDate());
		field.setResolution(resolution);
		field.setDateFormat(displayFormat);
		this.parameterFormat = parameterFormat;
	}

	public ReportParameterDateTime(String caption, String parameterName)
	{
		super(parameterName);
		field = new DateField(caption, new DateTime().withTimeAtStartOfDay().toDate());
		field.setResolution(Resolution.DAY);
		field.setDateFormat("yyyy/MM/dd");

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
}
