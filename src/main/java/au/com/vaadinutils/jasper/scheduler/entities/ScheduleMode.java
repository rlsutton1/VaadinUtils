package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.IOException;

import javax.mail.internet.AddressException;

import org.apache.commons.mail.EmailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.scheduler.ReportEmailRunner;
import au.com.vaadinutils.jasper.scheduler.ReportEmailSchedule;
import au.com.vaadinutils.jasper.scheduler.ReportEmailScheduleProvider;
import au.com.vaadinutils.jasper.scheduler.ScheduleTriState;

public enum ScheduleMode
{

	ONE_TIME
	{
		@Override
		public void checkDateAndRunScheduledReport(ReportEmailSchedule schedule, DateTime now,
				ReportEmailRunner reportRunner, JasperEmailSettings emailSettings,
				ReportEmailScheduleProvider scheduleProvider) throws AddressException, InterruptedException,
				IOException, EmailException, InstantiationException, IllegalAccessException, ClassNotFoundException
		{
			if (schedule.getOneTimeRunDateTime() == null)
			{
				schedule.setLastRuntime(now.toDate(), "Doesn't have a OneTimeRunDateTime set");
				schedule.setEnabled(false);
			}
			else if (schedule.getOneTimeRunDateTime().before(now.toDate()))
			{
				if (reportRunner.runReport(schedule, schedule.getOneTimeRunDateTime(), emailSettings))
				{
					schedule.setLastRuntime(now.toDate(), "Report successfully run");

					scheduleProvider.delete(schedule);
				}
				else
				{
					logger.warn("Report queue is not empty, will try scheduled report again later " + schedule);
				}
			}

		}
	},
	DAY_OF_WEEK
	{
		@Override
		public void checkDateAndRunScheduledReport(ReportEmailSchedule schedule, DateTime now,
				ReportEmailRunner reportRunner, JasperEmailSettings emailSettings,
				ReportEmailScheduleProvider scheduleProvider) throws AddressException, InterruptedException,
				IOException, EmailException, InstantiationException, IllegalAccessException, ClassNotFoundException
		{
			DateTime selectedScheduleTime = checkScheduleForTimeOfDay(schedule, now);
			if (selectedScheduleTime != null)
			{
				ScheduleTriState checkDayOfWeekSchedule = checkDayOfWeekSchedule(schedule, now);
				if (checkDayOfWeekSchedule == ScheduleTriState.FOUND)
				{
					if (reportRunner.runReport(schedule, selectedScheduleTime.toDate(), emailSettings))
					{
						schedule.setLastRuntime(now.toDate(), "Report successfully run");
					}
					else
					{
						logger.warn("Report queue is not empty, will try scheduled report again later " + schedule);
					}

				}
				else if (checkDayOfWeekSchedule == ScheduleTriState.NONE_EXIST)
				{
					schedule.setLastRuntime(now.toDate(), "No valid schedule");
					schedule.setEnabled(false);
				}
			}

		}
	},
	DAY_OF_MONTH
	{
		@Override
		public void checkDateAndRunScheduledReport(ReportEmailSchedule schedule, DateTime now,
				ReportEmailRunner reportRunner, JasperEmailSettings emailSettings,
				ReportEmailScheduleProvider scheduleProvider) throws AddressException, InterruptedException,
				IOException, EmailException, InstantiationException, IllegalAccessException, ClassNotFoundException
		{
			DateTime selectedScheduleTime = checkScheduleForTimeOfDay(schedule, now);

			if (selectedScheduleTime != null)
			{
				ScheduleTriState checkDayOfMonthSchedule = checkDayOfMonthSchedule(schedule, now);
				if (checkDayOfMonthSchedule == ScheduleTriState.FOUND)
				{
					if (reportRunner.runReport(schedule, selectedScheduleTime.toDate(), emailSettings))
					{
						schedule.setLastRuntime(now.toDate(), "Report successfully run");
					}
					else
					{
						logger.warn("Report queue is not empty, will try scheduled report again later " + schedule);
					}
				}
				else if (checkDayOfMonthSchedule == ScheduleTriState.NONE_EXIST)
				{
					schedule.setLastRuntime(now.toDate(), "No valid schedule");
					schedule.setEnabled(false);
				}
			}

		}
	},
	EVERY_DAY
	{
		@Override
		public void checkDateAndRunScheduledReport(ReportEmailSchedule schedule, DateTime now,
				ReportEmailRunner reportRunner, JasperEmailSettings emailSettings,
				ReportEmailScheduleProvider scheduleProvider) throws AddressException, InterruptedException,
				IOException, EmailException, InstantiationException, IllegalAccessException, ClassNotFoundException
		{
			DateTime selectedScheduleTime = checkScheduleForTimeOfDay(schedule, now);
			if (selectedScheduleTime != null)
			{
				if (reportRunner.runReport(schedule, selectedScheduleTime.toDate(), emailSettings))
				{
					schedule.setLastRuntime(now.toDate(), "Report successfully run");
				}
				else
				{
					logger.warn("Report queue is not empty, will try scheduled report again later " + schedule);
				}
			}

		}
	};

	Logger logger = LogManager.getLogger();

	ScheduleTriState checkDayOfWeekSchedule(ReportEmailSchedule schedule, DateTime now)
	{
		ScheduleTriState dayOfWeekSchedule = ScheduleTriState.NONE_EXIST;
		String scheduledDaysOfWeek = schedule.getScheduledDaysOfWeek();
		if (scheduledDaysOfWeek.length() > 0)
		{
			String[] days = scheduledDaysOfWeek.split(",");
			dayOfWeekSchedule = ScheduleTriState.NOT_SCHEDULED_NOW;
			for (String day : days)
			{
				if (day.equals("" + now.getDayOfWeek()))
				{
					dayOfWeekSchedule = ScheduleTriState.FOUND;
					break;
				}
			}
		}
		return dayOfWeekSchedule;
	}

	ScheduleTriState checkDayOfMonthSchedule(ReportEmailSchedule schedule, DateTime now)
	{
		ScheduleTriState dayOfMonthSchedule = ScheduleTriState.NONE_EXIST;

		if (schedule.getScheduledDayOfMonth() != null)
		{
			dayOfMonthSchedule = ScheduleTriState.NOT_SCHEDULED_NOW;
			if (schedule.getScheduledDayOfMonth() == now.getDayOfMonth())
			{
				dayOfMonthSchedule = ScheduleTriState.FOUND;
			}
		}
		return dayOfMonthSchedule;
	}

	DateTime checkScheduleForTimeOfDay(ReportEmailSchedule schedule, DateTime now)
	{
		boolean foundTimeSchedule = false;
		DateTime lastRun = null;
		if (schedule.getLastRuntime() != null)
		{
			lastRun = new DateTime(schedule.getLastRuntime());
		}
		DateTime selectedScheduleTime = null;

		DateTime scheduledDateTime = new DateTime(schedule.getTimeOfDayToRun());
		int scheduledMinute = scheduledDateTime.getMinuteOfHour();
		int scheduledHour = scheduledDateTime.getHourOfDay();

		DateTime scheduledTime = now.withMinuteOfHour(scheduledMinute).withHourOfDay(scheduledHour)
				.withSecondOfMinute(0).withMillisOfSecond(0);

		if (scheduledTime != null)
		{

			if (scheduledTime.isBefore(now))
			{
				// check the schedule is after the last run time
				if (lastRun == null || lastRun.isBefore(scheduledTime))
				{
					selectedScheduleTime = scheduledTime;
				}
				else
				{
					logger.debug("lastRun " + lastRun + " not before now:" + scheduledTime);
				}
			}
			else
			{
				logger.debug("scheduledTime " + scheduledTime + " not before now:" + now);
			}
		}
		else
		{
			throw new RuntimeException("No valid run time");
		}
		return selectedScheduleTime;
	}

	abstract public void checkDateAndRunScheduledReport(ReportEmailSchedule schedule, DateTime now,
			ReportEmailRunner reportRunner, JasperEmailSettings emailSettings,
			ReportEmailScheduleProvider scheduleProvider) throws AddressException, InterruptedException, IOException,
			EmailException, InstantiationException, IllegalAccessException, ClassNotFoundException;

}
