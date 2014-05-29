package au.com.vaadinutils.jasper.scheduler.entities;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import au.com.vaadinutils.jasper.scheduler.ReportEmailSchedule;

import com.google.common.base.Preconditions;

public enum ScheduleMode
{

	ONE_TIME("Run once only")
	{

		@Override
		public Date getNextRuntime(ReportEmailSchedule schedule, Date now)
		{
			return schedule.getOneTimeRunDateTime();
		}
	},
	DAY_OF_WEEK("Week days")
	{

		@Override
		public Date getNextRuntime(ReportEmailSchedule schedule, Date now)
		{
			String[] days = schedule.getScheduledDaysOfWeek().split(",");
			Preconditions.checkArgument(days.length > 0);
			List<Integer> intDays = new LinkedList<Integer>();
			for (String day : days)
			{
				int iDay = Integer.parseInt(day);
				Preconditions.checkArgument(iDay > 0 && iDay < 8);
				intDays.add(iDay);
			}
			Collections.sort(intDays);

			DateTime result = getFirstPossibleTime(schedule, now);

			while (!intDays.contains(result.getDayOfWeek()))
			{
				result = result.plusDays(1);
			}

			return result.toDate();

		}

	},
	DAY_OF_MONTH("Monthly")
	{

		@Override
		public Date getNextRuntime(ReportEmailSchedule schedule, Date now)
		{
			Integer day = schedule.getScheduledDayOfMonth();
			Preconditions.checkArgument(day > 0 && day < 32);
			DateTime result = getFirstPossibleTime(schedule, now);

			if (result.getDayOfMonth() > day)
			{
				// move to next month
				result = result.withDayOfMonth(1).plusMonths(1);
			}
			if (result.getDayOfMonth() < day)
			{
				// move to correct day
				int lastDayOfMonth = result.withDayOfMonth(1).plusMonths(1).minusDays(1).getDayOfMonth();
				result = result.withDayOfMonth(Math.min(day, lastDayOfMonth));
			}

			return result.toDate();
		}
	},
	EVERY_DAY("Daily")
	{

		@Override
		public Date getNextRuntime(ReportEmailSchedule schedule, Date now)
		{
			return getFirstPossibleTime(schedule, now).toDate();

		}
	};

	Logger logger = LogManager.getLogger();

	String name;

	ScheduleMode(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	static private DateTime getFirstPossibleTime(ReportEmailSchedule schedule, Date now)
	{
		DateTime scheduleTime = new DateTime(schedule.getTimeOfDayToRun());
		DateTime result = new DateTime(now);
		scheduleTime = scheduleTime.withDate(result.getYear(), result.getMonthOfYear(), result.getDayOfMonth());

		if (scheduleTime.isBefore(result))
		{
			result = result.plusDays(1);
		}
		result = result.withTime(scheduleTime.getHourOfDay(), scheduleTime.getMinuteOfHour(),
				scheduleTime.getSecondOfMinute(), 0);
		return result;
	}

	abstract public Date getNextRuntime(ReportEmailSchedule schedule, Date now);
}
