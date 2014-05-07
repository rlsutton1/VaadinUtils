package au.com.vaadinutils.jasper.scheduler;

import java.util.List;

public interface ReportEmailScheduleProvider
{

	List<ReportEmailSchedule> getSchedules();


	void delete(ReportEmailSchedule schedule);
}
