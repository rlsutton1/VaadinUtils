package au.com.vaadinutils.jasper.scheduler;

import java.util.List;

import javax.mail.Address;

import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

public abstract class ReportEmailScheduleTestAdaptor implements ReportEmailSchedule
{

	
	@Override
	public String getReportTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReportFileName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReportEmailRecipient> getRecipients()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String subject()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String message()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReportEmailParameter> getReportParameters()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Address getSendersEmailAddress()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScheduledDateParameter> getDateParameters()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends JasperReportProperties> getJasperReportPropertiesClass()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
