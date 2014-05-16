package au.com.vaadinutils.jasper.scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScheduleProvider implements ReportEmailScheduleProvider
{
	List<ReportEmailSchedule> list = new CopyOnWriteArrayList<ReportEmailSchedule>();


	ScheduleProvider(ReportEmailSchedule schedules[])
	{
		for (ReportEmailSchedule sched:schedules)
		{
			list.add(sched);
		}
	}

	@Override
	public List<ReportEmailSchedule> getSchedules()
	{

		
		
		return list;
	}



	@Override
	public void delete(ReportEmailSchedule schedule)
	{
		System.out.println("Delete schedule " + schedule + " requested");
		list.remove(schedule);

	}
}