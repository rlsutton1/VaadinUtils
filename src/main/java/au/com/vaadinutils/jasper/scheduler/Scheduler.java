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
						DateTime selectedScheduleTime = null;
						schedule.getScheduleMode().checkDateAndRunScheduledReport(schedule,now,reportRunner, emailSettings, scheduleProvider);
						
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
