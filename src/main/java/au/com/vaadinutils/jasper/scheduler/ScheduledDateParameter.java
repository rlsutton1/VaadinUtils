package au.com.vaadinutils.jasper.scheduler;

import java.util.Date;

import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;

public interface  ScheduledDateParameter
{
	public DateParameterType getType();
	public Date getDate();
	public String getName();
	public DateParameterOffsetType getOffsetType();
	public String getLabel();
	public void setDate(Date value);
	public void setOffsetType(DateParameterOffsetType value);
	
}
