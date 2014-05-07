package au.com.vaadinutils.jasper.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.jasper.JasperEmailSettings;

public class Scheduler implements Runnable
{
	Logger logger = LogManager.getLogger();

	final private ReportEmailScheduleProvider scheduleProvider;
	final private ScheduledExecutorService schedulerpool = Executors.newScheduledThreadPool(1);

	final private ReportEmailRunner reportRunner;

	final JasperEmailSettings emailSettings;

	final private ScheduledFuture<?> future;

	private DBmanager dbManager;

	Scheduler(ReportEmailScheduleProvider scheduleProvider, ReportEmailRunner reportRunner,
			JasperEmailSettings emailSettings, DBmanager dbManager)
	{

		this.scheduleProvider = scheduleProvider;
		this.reportRunner = reportRunner;
		this.emailSettings = emailSettings;
		this.dbManager = dbManager;

		future = schedulerpool.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
	}

	private Calendar getCalendar(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	@Override
	public synchronized void run()
	{
		try
		{
			protectedRun();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	public void protectedRun()
	{
		try
		{
			Thread.currentThread().setName("Jasper Report Scheduler");
			dbManager.beginDbTransaction();
			List<ReportEmailSchedule> schedules = scheduleProvider.getSchedules();
			for (ReportEmailSchedule schedule : schedules)
			{
				if (schedule.isEnabled())
				{
					try
					{
						Calendar now = Calendar.getInstance();
						Calendar selectedScheduleTime = checkScheduleForTimeOfDay(schedule, now);

						switch (schedule.getScheduleMode())
						{
						case DAY_OF_MONTH:
							if (selectedScheduleTime == null)
							{
								schedule.setLastRuntime(now.getTime(), "No valid run time");
								schedule.setEnabled(false);
							}
							else
							{
								ScheduleTriState checkDayOfMonthSchedule = checkDayOfMonthSchedule(schedule, now);
								if (checkDayOfMonthSchedule == ScheduleTriState.FOUND)
								{
									if (reportRunner.runReport(schedule, selectedScheduleTime.getTime(), emailSettings))
									{
										schedule.setLastRuntime(now.getTime(), "Report successfully run");
									}
									else
									{
										logger.warn("Report queue is not empty, will try scheduled report again later "
												+ schedule);
									}
								}
								else if (checkDayOfMonthSchedule == ScheduleTriState.NONE_EXIST)
								{
									schedule.setLastRuntime(now.getTime(), "No valid schedule");
									schedule.setEnabled(false);
								}
							}

							break;
						case DAY_OF_WEEK:
							if (selectedScheduleTime == null)
							{
								schedule.setLastRuntime(now.getTime(), "No valid run time");
								schedule.setEnabled(false);
							}
							else
							{
								ScheduleTriState checkDayOfWeekSchedule = checkDayOfWeekSchedule(schedule, now);
								if (checkDayOfWeekSchedule == ScheduleTriState.FOUND)
								{
									if (reportRunner.runReport(schedule, selectedScheduleTime.getTime(), emailSettings))
									{
										schedule.setLastRuntime(now.getTime(), "Report successfully run");
									}
									else
									{
										logger.warn("Report queue is not empty, will try scheduled report again later "
												+ schedule);
									}

								}
								else if (checkDayOfWeekSchedule == ScheduleTriState.NONE_EXIST)
								{
									schedule.setLastRuntime(now.getTime(), "No valid schedule");
									schedule.setEnabled(false);
								}
							}
							break;
						case EVERY_DAY:
							if (selectedScheduleTime == null)
							{
								schedule.setLastRuntime(now.getTime(), "No valid run time");
								schedule.setEnabled(false);
							}
							else
							{
								if (reportRunner.runReport(schedule, selectedScheduleTime.getTime(), emailSettings))
								{
									schedule.setLastRuntime(now.getTime(), "Report successfully run");
								}
								else
								{
									logger.warn("Report queue is not empty, will try scheduled report again later "
											+ schedule);
								}
							}
							break;
						case ONE_TIME:
							if (schedule.getOneTimeRunDateTime() == null)
							{
								schedule.setLastRuntime(now.getTime(), "Doesn't have a OneTimeRunDateTime set");
								schedule.setEnabled(false);
							}
							else if (schedule.getOneTimeRunDateTime().before(now.getTime()))
							{
								if (reportRunner.runReport(schedule, schedule.getOneTimeRunDateTime(), emailSettings))
								{
									schedule.setLastRuntime(now.getTime(), "Report successfully run");
									
									scheduleProvider.delete(schedule);
								}
								else
								{
									logger.warn("Report queue is not empty, will try scheduled report again later "
											+ schedule);
								}
							}
							break;
						}

					}
					catch (Exception e)
					{
						schedule.setEnabled(false);
						schedule.setLastRuntime(new Date(), e.getMessage());
						logger.error(e, e);
					}
				}
			}

		}

		finally
		{
			dbManager.commitDbTransaction();

		}

	}

	private ScheduleTriState checkDayOfWeekSchedule(ReportEmailSchedule schedule, Calendar now)
	{
		ScheduleTriState dayOfWeekSchedule = ScheduleTriState.NONE_EXIST;
		if (schedule.getScheduledDaysOfWeek().length() > 0)
		{
			dayOfWeekSchedule = ScheduleTriState.NOT_SCHEDULED_NOW;
			if (schedule.getScheduledDaysOfWeek().contains("" + now.get(Calendar.DAY_OF_WEEK)))
			{
				dayOfWeekSchedule = ScheduleTriState.FOUND;
			}
		}
		return dayOfWeekSchedule;
	}

	private ScheduleTriState checkDayOfMonthSchedule(ReportEmailSchedule schedule, Calendar now)
	{
		ScheduleTriState dayOfMonthSchedule = ScheduleTriState.NONE_EXIST;

		if (schedule.getScheduledDayOfMonth() != null)
		{
			dayOfMonthSchedule = ScheduleTriState.NOT_SCHEDULED_NOW;
			if (schedule.getScheduledDayOfMonth() == now.get(Calendar.DAY_OF_MONTH))
			{
				dayOfMonthSchedule = ScheduleTriState.FOUND;
			}
		}
		return dayOfMonthSchedule;
	}

	private Calendar checkScheduleForTimeOfDay(ReportEmailSchedule schedule, Calendar now)
	{
		boolean foundTimeSchedule = false;
		Calendar lastRun = null;
		if (schedule.getLastRuntime() != null)
		{
			lastRun = getCalendar(schedule.getLastRuntime());
		}
		Calendar selectedScheduleTime = null;
		Date dTime = schedule.getTimeOfDayToRun();
		Calendar time = null;
		if (dTime != null)
		{
			time = getCalendar(dTime);

			if (time.get(Calendar.HOUR_OF_DAY) <= now.get(Calendar.HOUR_OF_DAY))
			{
				if (time.get(Calendar.MINUTE) <= now.get(Calendar.MINUTE))
				{
					// check the schedule is after the last run time
					if (lastRun == null)
					{
						selectedScheduleTime = time;
					}
					else if (time.get(Calendar.HOUR_OF_DAY) >= lastRun.get(Calendar.HOUR_OF_DAY))
					{
						if (time.get(Calendar.MINUTE) > lastRun.get(Calendar.MINUTE))
						{
							selectedScheduleTime = time;

						}
					}

				}
			}
		}
		return selectedScheduleTime;
	}

	public void stop()
	{
		future.cancel(true);

	}

}
