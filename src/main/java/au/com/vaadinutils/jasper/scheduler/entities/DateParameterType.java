package au.com.vaadinutils.jasper.scheduler.entities;

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
}
