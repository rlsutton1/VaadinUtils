package au.com.vaadinutils.jasper.parameter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ReportParameterDateTimeOneDay extends ReportParameterDateTimeRange
{

	Logger logger = LogManager.getLogger();

	public ReportParameterDateTimeOneDay(String caption, String startParameterName, String endParameterName)
	{
		super(caption, startParameterName, endParameterName);
	}

	public ReportParameterDateTimeOneDay(String caption, String startParameterName, String endParameterName,
			Resolution resolution, String displayFormat, String parameterFormat)
	{
		super(caption, startParameterName, endParameterName, resolution, displayFormat, parameterFormat);
	}

	public Date getDate(String parameterName)
	{
		Date value;
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			value = startfield.getValue();
		}
		else
		{
			value = new DateTime(startfield.getValue()).plusDays(1).toDate();

		}
		return value;
	}

	@Override
	public Component getComponent()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(startfield);
		return layout;
	}

	@Override
	public void setValueAsString(String value, String parameterName) throws ReadOnlyException, ConversionException,
			ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(parameterFormat);
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			startfield.setValue(sdf.parse(value));
		}
		logger.warn("Trying to set end date for a one day date range, ignoring");
	}

	public Date getEndDate()
	{
		return getDate(endParameterName);
	}

	public void setEndDate(Date date)
	{

	}

}
