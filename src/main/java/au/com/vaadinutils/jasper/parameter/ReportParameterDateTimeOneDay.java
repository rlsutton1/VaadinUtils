package au.com.vaadinutils.jasper.parameter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ReportParameterDateTimeOneDay extends ReportParameterDateTimeRange
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	public ReportParameterDateTimeOneDay(String caption, String startParameterName, String endParameterName,
			Resolution resolution, String displayFormat, String parameterFormat)
	{
		super(caption, startParameterName, endParameterName, resolution, displayFormat, parameterFormat, 0);
	}

	@Override
	public Component getComponent()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(startfield);
		return layout;
	}

	@Override
	public void setValueAsString(String value, String parameterName)
			throws ReadOnlyException, ConversionException, ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(parameterFormat);
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			startfield.setValue(sdf.parse(value));
		}
		logger.warn("Trying to set end date for a one day date range, ignoring");
	}

	@Override
	public Date getEndDate()
	{
		return new DateTime(getDate(startParameterName).getTime()).plusDays(1).toDate();
	}

	@Override
	public Date getDate(String parameterName)
	{
		Date value;
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			value = startfield.getValue();
		}
		else if (parameterName.equalsIgnoreCase(endParameterName))
		{
			value = new DateTime(startfield.getValue()).plusDays(0).toDate();
		}
		else
		{
			throw new RuntimeException("Attempt to retrieve invalid parameter name " + parameterName
					+ " valid names are " + startParameterName + "," + endParameterName);
		}
		return value;
	}

	@Override
	public String getValue(String parameterName)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(parameterFormat);
		if (parameterName.equalsIgnoreCase(endParameterName))
		{
			return sdf.format(new DateTime(getDate(startParameterName).getTime()).plusDays(1).toDate());
		}

		return sdf.format(new DateTime(getDate(parameterName).getTime()).withTimeAtStartOfDay().toDate());
	}

	@Override
	public void setEndDate(Date date)
	{

	}

}
