package au.com.vaadinutils.jasper.scheduler;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.events.CrudEventDistributer;
import au.com.vaadinutils.crud.events.CrudEventListener;
import au.com.vaadinutils.crud.events.CrudEventType;
import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;

public class Scheduler implements Runnable
{
	public  static final String REPORT_SUCCESSFULLY_RUN = "Report successfully run";

	Logger logger = LogManager.getLogger();

	final private ReportEmailScheduleProvider scheduleProvider;
	final private ScheduledExecutorService schedulerpool = Executors.newScheduledThreadPool(1);

	final private ReportEmailRunner reportRunner;

	final JasperEmailSettings emailSettings;

	final private ScheduledFuture<?> future;

	private DBmanager dbManager;

	// the soonest time any report needs to be run
	AtomicReference<DateTime> next = new AtomicReference<DateTime>();

	Scheduler(ReportEmailScheduleProvider scheduleProvider, ReportEmailRunner reportRunner,
			JasperEmailSettings emailSettings, DBmanager dbManager)
	{
		next.set(new DateTime());
		this.scheduleProvider = scheduleProvider;
		this.reportRunner = reportRunner;
		this.emailSettings = emailSettings;
		this.dbManager = dbManager;

		future = schedulerpool.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);

		CrudEventDistributer.addListener(JasperReportScheduleLayout.class, new CrudEventListener()
		{

			@Override
			public void crudEvent(CrudEventType event, CrudEntity entity)
			{
				next.set(new DateTime());

			}
		});
	}

	public void reschedule()
	{
		next.set(new DateTime());
	}

	@Override
	public synchronized void run()
	{
		try
		{
			if (next.get().isBeforeNow())
			{
				protectedRun();
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	private void protectedRun()
	{
		try
		{
			DateTime currentNext = next.get();
			DateTime nextPossible = new DateTime().plusDays(1).withTimeAtStartOfDay();
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
								schedule.setLastRuntime(now.toDate(), REPORT_SUCCESSFULLY_RUN);
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
					if (nextPossible.isAfter(schedule.getNextScheduledTime().getTime()))
					{
						nextPossible = new DateTime(schedule.getNextScheduledTime().getTime());
					}
				}
			}

			if (!next.compareAndSet(currentNext, nextPossible))
			{
				// next was updated while we were running, so run again as soon
				// as possible
				next.set(new DateTime());
			}

		}

		finally
		{
			dbManager.commitDbTransaction();

		}

	}

	public void stop()
	{
		schedulerpool.shutdown();
		future.cancel(true);

	}

}
