package au.com.vaadinutils.jasper.scheduler;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;

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
						DateTime now = new DateTime();

						Date nextScheduledTime = schedule.getNextScheduledTime();
						if (nextScheduledTime == null)
						{
							// upgrate existing schedules that dont have a next
							// runtime
							nextScheduledTime = schedule.getScheduleMode().getNextRuntime(schedule, now.toDate());
							schedule.setNextScheduledRunTime(nextScheduledTime);
						}

						Date lastRuntime = schedule.getLastRuntime();
						if ((lastRuntime == null || lastRuntime.before(nextScheduledTime))
								&& nextScheduledTime.before(now.toDate()))
						{
							if (reportRunner.runReport(schedule, nextScheduledTime, emailSettings))
							{
								schedule.setLastRuntime(now.toDate(), "Report successfully run");
								if (schedule.getScheduleMode() == ScheduleMode.ONE_TIME)
								{
									scheduleProvider.delete(schedule);
								}
								else
								{
									schedule.setNextScheduledRunTime(schedule.getScheduleMode().getNextRuntime(
											schedule, now.toDate()));
								}
							}
							else
							{
								logger.warn("Report queue is not empty, will try scheduled report again later "
										+ schedule);
							}
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

	public void stop()
	{
		future.cancel(true);

	}

}
