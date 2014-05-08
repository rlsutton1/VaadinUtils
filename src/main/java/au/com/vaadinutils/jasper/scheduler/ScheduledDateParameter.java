package au.com.vaadinutils.jasper.scheduler;

import java.util.Date;

import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

public interface  ScheduledDateParameter
{
	public DateParameterType getType();
	public DateParameterOffsetType getOffsetType();
	public String getLabel();
	public void setStartDate(Date value);
	public Date getStartDate();
	public String getStartName();
	public void setEndDate(Date value);
	public Date getEndDate();
	public String getEndName();

	public void setOffsetType(DateParameterOffsetType value);
	
}
