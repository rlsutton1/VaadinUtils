package au.com.vaadinutils.jasper.scheduler.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

public enum DateParameterType
{
	DATE("yyyy/MM/dd"), DATE_TIME("yyyy/MM/dd HH:mm:ss");

	private String dateFormat;

	DateParameterType(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public String getDateFormat()
	{
		return dateFormat;
	}

	public String format(Date date)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(getDateFormat());

		return formatter.format(date);
	}
}
