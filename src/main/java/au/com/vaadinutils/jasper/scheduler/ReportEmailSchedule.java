package au.com.vaadinutils.jasper.scheduler;


import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.scheduler.entities.ScheduleMode;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

public interface ReportEmailSchedule 
{
	// Logger logger = LogManager.getLogger();
	

	// report title, will appear on the report
	String getReportTitle();
	
	// file name of the jrxml 
	String getReportFileName();
	
	// list of email addresses
	List<ReportEmailRecipient> getRecipients() throws AddressException;
	
	// subject for the email, allows some date variables %d
	String subject();
	
	// message to go in the body of the email
	String message();
	
	// key = Report Parameter name, value = is the parameter value
Collection<ReportEmailParameter> getReportParameters();
	
	// days of week  0 = sunday
	String getScheduledDaysOfWeek();

	// days of month
	Integer getScheduledDayOfMonth();
	
	Date getTimeOfDayToRun();
	
	// date to run this report if it's not recurring
	Date getOneTimeRunDateTime();
	
	ScheduleMode getScheduleMode();
	
	// used to determine if this report is due to run
	Date getLastRuntime();
	
	// called after the report is successfully run.
	void setLastRuntime(Date date,String auditDetails);

	Address getSendersEmailAddress() throws AddressException;

	List<ScheduledDateParameter> getDateParameters();

	Class<? extends JasperReportProperties> getJasperReportPropertiesClass() throws ClassNotFoundException;

	void setEnabled(boolean b);

	boolean isEnabled();

	String getSendersUsername();

	// returns the time that this report should run next
	Date getNextScheduledTime();

	void setNextScheduledRunTime(Date nextRuntime);

	OutputFormat getOutputFormat();

	
	
}
