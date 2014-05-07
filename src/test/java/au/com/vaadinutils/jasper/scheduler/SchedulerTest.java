package au.com.vaadinutils.jasper.scheduler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.junit.Test;

import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;

public class SchedulerTest
{
	private Calendar scheduledTime;
	private int runInvoked;

	@Test
	public void test()
	{

		ReportEmailScheduleProvider provider = new ReportEmailScheduleProvider()
		{
			ReportEmailSchedule schedule = new ReportEmailScheduleTestAdaptor()
			{

				private Date lastRuntime = null;

				@Override
				public String getScheduledDaysOfWeek()
				{
					return "6";
				}

				@Override
				public Integer getScheduledDayOfMonth()
				{

					return 2;
				}

				@Override
				public Date getTimeOfDayToRun()
				{

					return scheduledTime.getTime();
				}

				@Override
				public Date getOneTimeRunDateTime()
				{

					return scheduledTime.getTime();
				}

				@Override
				public ScheduleMode getScheduleMode()
				{
					return ScheduleMode.ONE_TIME;
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
			};

			@Override
			public List<ReportEmailSchedule> getSchedules()
			{

				List<ReportEmailSchedule> list = new LinkedList<ReportEmailSchedule>();
				list.add(schedule);
				return list;
			}



			@Override
			public void delete(ReportEmailSchedule schedule)
			{
				System.out.println("Delete schedule " + schedule + " requested");

			}
		};
		ReportEmailRunner runner = new ReportEmailRunner()
		{

			@Override
			public boolean runReport(ReportEmailSchedule schedule, Date scheduleTime, JasperEmailSettings emailSettings)
					throws InterruptedException, IOException, EmailException, InstantiationException,
					IllegalAccessException
			{
				runInvoked++;
				return true;
			}
		};

		scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.SECOND, 3);
		DBmanager dbManager = new DBmanager()
		{
			
			@Override
			public void commitDbTransaction()
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beginDbTransaction()
			{
				// TODO Auto-generated method stub
				
			}
		};
		Scheduler scheduler = new Scheduler(provider, runner, null, dbManager );
		for (int i = 0; i < 2; i++)
		{
			scheduler.run();
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{

			}
		}

		assertTrue("invoked " + runInvoked + " times", runInvoked == 1);
	}
	// Logger logger = LogManager.getLogger();
}
