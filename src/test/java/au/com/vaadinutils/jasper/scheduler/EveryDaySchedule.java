package au.com.vaadinutils.jasper.scheduler;

import java.util.Date;

import org.joda.time.DateTime;

import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;

public class EveryDaySchedule extends ReportEmailScheduleTestAdaptor
{

	private Date lastRuntime = null;
	final DateTime scheduledTime ;

	EveryDaySchedule(Date date)
	{
		scheduledTime = new DateTime(date);
	}
	
	@Override
	public String getScheduledDaysOfWeek()
	{
		return null;
	}

	@Override
	public Integer getScheduledDayOfMonth()
	{

		return null;
	}

	@Override
	public Date getTimeOfDayToRun()
	{

		return scheduledTime.toDate();
	}

	@Override
	public Date getOneTimeRunDateTime()
	{

		return null;
	}

	@Override
	public ScheduleMode getScheduleMode()
	{
		return ScheduleMode.EVERY_DAY;
	}

	@Override
	public Date getLastRuntime()
	{

		return lastRuntime;
	}

	@Override
	public void setLastRuntime(Date date, String auditDetails)
	{
		lastRuntime = date;
		System.out.println(auditDetails);

	}

	@Override
	public void setEnabled(boolean b)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isEnabled()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getSendersUsername()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
