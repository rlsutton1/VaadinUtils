package au.com.vaadinutils.jasper.scheduler;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.mail.EmailException;

import au.com.vaadinutils.jasper.JasperEmailSettings;

public class ReportRunner implements ReportEmailRunner
{

	volatile private int runInvoked;

	@Override
	public boolean runReport(ReportEmailSchedule schedule, Date scheduleTime, JasperEmailSettings emailSettings)
			throws InterruptedException, IOException, EmailException, InstantiationException,
			IllegalAccessException
	{
		runInvoked++;
		return true;
	}
	
	int getInvocations()
	{
		return runInvoked;
	}

}
