package au.com.vaadinutils.jasper.scheduler;

import java.util.List;

import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

public abstract class ReportEmailScheduleTestAdaptor implements ReportEmailSchedule
{

	@Override
	public String getReportTitle()
	{
		return null;
	}

	@Override
	public String getReportFileName()
	{
		return null;
	}

	@Override
	public List<ReportEmailRecipient> getRecipients()
	{
		return null;
	}

	@Override
	public String subject()
	{
		return null;
	}

	@Override
	public String message()
	{
		return null;
	}

	@Override
	public List<ReportEmailParameter> getReportParameters()
	{
		return null;
	}

	@Override
	public List<ScheduledDateParameter> getDateParameters()
	{
		return null;
	}

	@Override
	public Class<? extends JasperReportProperties> getJasperReportPropertiesClass()
	{
		return null;
	}

}
