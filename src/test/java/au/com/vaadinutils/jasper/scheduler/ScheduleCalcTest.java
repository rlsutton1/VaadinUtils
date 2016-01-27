package au.com.vaadinutils.jasper.scheduler;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;

public class ScheduleCalcTest
{
	// Logger logger = LogManager.getLogger();

	@Test
	public void testDaysOfWeek() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		Date scheduledTime = sdf.parse("14/05/29 09:30:00");
		String daysOfWeek = "1,3,5"; // mon, wed & fri

		ReportEmailSchedule schedule = new DayOfWeekSchedule(scheduledTime, daysOfWeek);

		Map<Date, Date> testTimes = new HashMap<Date, Date>();
		testTimes.put(sdf.parse("14/05/29 10:00:00"), sdf.parse("14/05/30 09:30:00")); // Thurs/Fri
		testTimes.put(sdf.parse("14/05/30 10:00:00"), sdf.parse("14/06/02 09:30:00")); // Fri/Mon
		testTimes.put(sdf.parse("14/05/31 10:00:00"), sdf.parse("14/06/02 09:30:00")); // Sat/Mon
		testTimes.put(sdf.parse("14/06/01 10:00:00"), sdf.parse("14/06/02 09:30:00")); // Sun/Mon
		testTimes.put(sdf.parse("14/06/02 10:00:00"), sdf.parse("14/06/04 09:30:00")); // Mon/Wed
		testTimes.put(sdf.parse("14/06/03 10:00:00"), sdf.parse("14/06/04 09:30:00")); // Tues/Wed
		testTimes.put(sdf.parse("14/06/04 10:00:00"), sdf.parse("14/06/06 09:30:00")); // Wed/Fri

		for (Entry<Date, Date> testTime : testTimes.entrySet())
		{
			Date now = testTime.getKey();
			Date scheduled = ScheduleMode.DAY_OF_WEEK.getNextRuntime(schedule, now);

			Date expectedResult = testTime.getValue();
			assertTrue("expected " + expectedResult + " got " + scheduled, scheduled.equals(expectedResult));

		}

	}

	@Test
	public void testDayOfMonth() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		Date scheduledTime = sdf.parse("14/05/29 09:30:00");
		// String daysOfMonth = "1,3,5"; // mon, wed & fri

		ReportEmailSchedule schedule = new DayOfMonthSchedule(scheduledTime, 29);

		Map<Date, Date> testTimes = new HashMap<Date, Date>();
		testTimes.put(sdf.parse("14/01/28 10:00:00"), sdf.parse("14/01/29 09:30:00")); // Thurs/Fri
		testTimes.put(sdf.parse("14/02/25 10:00:00"), sdf.parse("14/02/28 09:30:00")); // Fri/Mon
		testTimes.put(sdf.parse("14/12/31 10:00:00"), sdf.parse("15/01/29 09:30:00")); // Sat/Mon

		for (Entry<Date, Date> testTime : testTimes.entrySet())
		{
			Date now = testTime.getKey();
			Date scheduled = ScheduleMode.DAY_OF_MONTH.getNextRuntime(schedule, now);

			Date expectedResult = testTime.getValue();
			assertTrue("expected " + expectedResult + " got " + scheduled, scheduled.equals(expectedResult));

		}

	}

	@Test
	public void testEveryDay() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		Date scheduledTime = sdf.parse("14/05/29 09:30:00");

		ReportEmailSchedule schedule = new EveryDaySchedule(scheduledTime);

		Map<Date, Date> testTimes = new HashMap<Date, Date>();
		testTimes.put(sdf.parse("14/05/29 10:00:00"), sdf.parse("14/05/30 09:30:00"));
		testTimes.put(sdf.parse("14/05/30 10:00:00"), sdf.parse("14/05/31 09:30:00"));
		testTimes.put(sdf.parse("14/05/31 10:00:00"), sdf.parse("14/06/01 09:30:00"));
		testTimes.put(sdf.parse("14/06/01 10:00:00"), sdf.parse("14/06/02 09:30:00"));
		testTimes.put(sdf.parse("14/06/02 10:00:00"), sdf.parse("14/06/03 09:30:00"));
		testTimes.put(sdf.parse("14/06/03 10:00:00"), sdf.parse("14/06/04 09:30:00"));
		testTimes.put(sdf.parse("14/06/04 10:00:00"), sdf.parse("14/06/05 09:30:00"));

		for (Entry<Date, Date> testTime : testTimes.entrySet())
		{
			Date now = testTime.getKey();
			Date scheduled = ScheduleMode.EVERY_DAY.getNextRuntime(schedule, now);

			Date expectedResult = testTime.getValue();
			assertTrue("expected " + expectedResult + " got " + scheduled, scheduled.equals(expectedResult));

		}

	}
}
