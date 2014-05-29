package au.com.vaadinutils.jasper.scheduler;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

public class SchedulerTest
{

	@Test
	public void testOneTimeOnly()
	{

		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.SECOND, -1);

		ReportRunner reportRunner = new ReportRunner();
		ReportEmailSchedule schedule = new OneTimeSchedule(scheduledTime.getTime());
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());
		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 1);
	}

	@Test
	public void testOneTimePremature()
	{

		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.MINUTE, 1);

		ReportRunner reportRunner = new ReportRunner();
		ReportEmailSchedule schedule = new OneTimeSchedule(scheduledTime.getTime());
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}
		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 0);
	}

	@Test
	public void testDayOfWeekCorrectDay() throws InterruptedException
	{

		DateTime scheduledTime = new DateTime();
		// scheduledTime.add(Calendar.SECOND, 3);

		int day = scheduledTime.getDayOfWeek();

		ReportRunner reportRunner = new ReportRunner();
		String daysOfWeek = "1," + day + ",5";
		ReportEmailSchedule schedule = new DayOfWeekSchedule(scheduledTime.plusSeconds(2).toDate(), daysOfWeek);
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		Thread.sleep(2000);
		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 1);
	}

	@Test
	public void testDayOfWeekCorrectDayBeforeHour()
	{

		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.MINUTE, 1);

		int day = scheduledTime.get(Calendar.DAY_OF_WEEK);

		ReportRunner reportRunner = new ReportRunner();
		String daysOfWeek = "1," + day + ",5";
		ReportEmailSchedule schedule = new DayOfWeekSchedule(scheduledTime.getTime(), daysOfWeek);
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 0);
	}

	@Test
	public void testDayOfMonthCorrectDay()
	{

		Calendar scheduledTime = Calendar.getInstance();
		// scheduledTime.add(Calendar.SECOND, 3);

		int day = scheduledTime.get(Calendar.DAY_OF_MONTH);

		ReportRunner reportRunner = new ReportRunner();

		ReportEmailSchedule schedule = new DayOfMonthSchedule(scheduledTime.getTime(), day);
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 1);
	}

	@Test
	public void testDayOfMonthCorrectDayBeforeHour()
	{

		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.MINUTE, 1);
		System.out.println(new Date());

		int day = scheduledTime.get(Calendar.DAY_OF_MONTH);

		ReportRunner reportRunner = new ReportRunner();

		ReportEmailSchedule schedule = new DayOfMonthSchedule(scheduledTime.getTime(), day);
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 0);
	}

	@Test
	public void testEveryDayCorrectDay()
	{

		Calendar scheduledTime = Calendar.getInstance();

		ReportRunner reportRunner = new ReportRunner();

		ReportEmailSchedule schedule = new EveryDaySchedule(scheduledTime.getTime());
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 1);
	}

	@Test
	public void testEveryDayCorrectDaySecondRun() throws InterruptedException
	{

		DateTime time = new DateTime();

		ReportRunner reportRunner = new ReportRunner();

		ReportEmailSchedule schedule = new EveryDaySchedule(time.plusSeconds(1).toDate());
		schedule.setLastRuntime(time.minusDays(1).toDate(), "");
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		Thread.sleep(2000);
		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 1);
	}

	@Test
	public void testEveryDayCorrectDayBeforeHour()
	{

		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.add(Calendar.MINUTE, 1);

		ReportRunner reportRunner = new ReportRunner();

		ReportEmailSchedule schedule = new EveryDaySchedule(scheduledTime.getTime());
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 0);
	}

	@Test
	public void testEveryDayCorrectDaySecondRunWrongHour()
	{

		DateTime time = new DateTime();

		ReportRunner reportRunner = new ReportRunner();

		ReportEmailSchedule schedule = new EveryDaySchedule(time.plusHours(1).toDate());
		schedule.setLastRuntime(time.minusDays(1).toDate(), "");
		ScheduleProvider scheduleProvider = new ScheduleProvider(new ReportEmailSchedule[] { schedule });
		Scheduler scheduler = new Scheduler(scheduleProvider, reportRunner, null, new DBmanagerprovider());

		for (int i = 0; i < 5; i++)
		{
			scheduler.run();

		}

		assertTrue("invoked " + reportRunner.getInvocations() + " times", reportRunner.getInvocations() == 0);
	}

}
