package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.scheduler.ReportEmailParameter;
import au.com.vaadinutils.jasper.scheduler.ReportEmailSchedule;
import au.com.vaadinutils.jasper.scheduler.ScheduledDateParameter;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.google.common.base.Preconditions;

@Entity
@Table(name = "tblreportemailschedule")
public class ReportEmailScheduleEntity implements Serializable, CrudEntity, ReportEmailSchedule
{
	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ReportEmailScheduleEntity [reportTitle=" + reportTitle + ", lastRuntime=" + lastRuntime + ", sender="
				+ sender + ", iID=" + iID + ", enabled=" + enabled + ", scheduleMode=" + scheduleMode
				+ ", oneTimeRunDateTime=" + oneTimeRunDateTime + ", timeOfDayToRun=" + timeOfDayToRun
				+ ", scheduledDayOfMonth=" + scheduledDayOfMonth + ", scheduledDaysOfWeek=" + scheduledDaysOfWeek
				+ ", message=" + message + ", subject=" + subject + ", reportLog=" + reportLog + ", reportFileName="
				+ reportFileName + ", JasperReportPropertiesClassName=" + JasperReportPropertiesClassName + "]";
	}

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	private String JasperReportPropertiesClassName;

	@OneToMany(cascade =
	{ CascadeType.REMOVE, CascadeType.PERSIST })
	private List<ReportEmailScheduledDateParameter> dateParameters = new LinkedList<>();

	boolean enabled = true;

	@OneToOne(cascade =
	{ CascadeType.REMOVE, CascadeType.PERSIST })
	private ReportEmailSender sender;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastRuntime;

	@Enumerated(EnumType.STRING)
	private ScheduleMode scheduleMode;

	@Temporal(TemporalType.TIMESTAMP)
	private Date oneTimeRunDateTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timeOfDayToRun;

	private Integer scheduledDayOfMonth;

	private String scheduledDaysOfWeek = "";

	@OneToMany(cascade =
	{ CascadeType.REMOVE, CascadeType.PERSIST })
	private List<ReportEmailParameterEntity> reportParameters;

	@NotNull
	@Enumerated(EnumType.STRING)
	private OutputFormat outputFormat;

	private String message;

	private String subject;

	/**
	 * error messages from the last time this report ran
	 */
	private String reportLog;

	@ManyToMany(cascade =
	{ CascadeType.PERSIST })
	private List<ReportEmailRecipient> recipients = new LinkedList<ReportEmailRecipient>();

	private String reportFileName;

	private String reportTitle;

	@SuppressWarnings("unused")
	private String reportIdentifier;

	@Temporal(TemporalType.TIMESTAMP)
	private Date nextScheduledTime;

	@Override
	public Long getId()
	{
		return iID;
	}

	@Override
	public void setId(Long id)
	{
		iID = id;

	}

	@Override
	public String getName()
	{
		return reportTitle;
	}

	@Override
	public String getReportTitle()
	{
		return reportTitle;
	}

	@Override
	public String getReportFileName()
	{
		return reportFileName;
	}

	@Override
	public List<ReportEmailRecipient> getRecipients()
	{

		return recipients;
	}

	@Override
	public String subject()
	{
		return subject;
	}

	@Override
	public String message()
	{
		return message;
	}

	@Override
	public Collection<ReportEmailParameter> getReportParameters()
	{
		List<ReportEmailParameter> tmp = new LinkedList<ReportEmailParameter>();
		tmp.addAll(reportParameters);

		return Collections.unmodifiableCollection(tmp);
	}

	@Override
	public String getScheduledDaysOfWeek()
	{
		return scheduledDaysOfWeek;
	}

	@Override
	public Integer getScheduledDayOfMonth()
	{
		return scheduledDayOfMonth;
	}

	@Override
	public Date getTimeOfDayToRun()
	{
		return timeOfDayToRun;
	}

	@Override
	public Date getOneTimeRunDateTime()
	{
		return oneTimeRunDateTime;
	}

	@Override
	public ScheduleMode getScheduleMode()
	{
		return scheduleMode;
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
		reportLog = auditDetails;

	}

	public boolean hasSenderEmailAddress()
	{
		return sender != null;
	}

	@Override
	public Address getSendersEmailAddress() throws AddressException
	{
		Preconditions.checkNotNull(sender, "You should call hasSenderEmailAddress first to check if there is a sender");
		return new InternetAddress(sender.getEmail());
	}

	@Override
	public List<ScheduledDateParameter> getDateParameters()
	{
		List<ScheduledDateParameter> tmp = new LinkedList<ScheduledDateParameter>();
		tmp.addAll(dateParameters);
		return tmp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends JasperReportProperties> getJasperReportPropertiesClass() throws ClassNotFoundException
	{
		return (Class<? extends JasperReportProperties>) Class.forName(JasperReportPropertiesClassName);
	}

	public void setTitle(String reportTitle2)
	{
		reportTitle = reportTitle2;

	}

	public void setReportFilename(String reportFileName2)
	{
		reportFileName = reportFileName2;

	}

	public void setMessage(String message)
	{
		this.message = message;

	}

	public void setSender(ReportEmailSender reportEmailSender)
	{
		this.sender = reportEmailSender;

	}

	public void setSubject(String subject)
	{
		this.subject = subject;

	}

	public void setReportClass(Class<? extends JasperReportProperties> reportClass)
	{
		JasperReportPropertiesClassName = reportClass.getName();

	}

	public void setScheduleMode(ScheduleMode scheduleMode)
	{
		this.scheduleMode = scheduleMode;

	}

	public void setOneTimeRunTime(Date date)
	{
		this.oneTimeRunDateTime = date;

	}

	public void setParameters(List<ReportEmailParameterEntity> rparams)
	{
		reportParameters = rparams;

	}

	public void setDateParameters(List<ReportEmailScheduledDateParameter> dparams)
	{
		dateParameters = dparams;

	}

	@Override
	public void setEnabled(boolean b)
	{
		enabled = b;

	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public String getSendersUsername()
	{
		return sender.username;
	}

	public void setRecipients(List<ReportEmailRecipient> recips)
	{
		recipients = recips;

	}

	public ReportEmailSender getSender()
	{
		return sender;
	}

	public void addReportParameter(ReportEmailParameterEntity reportEmailParameterEntity)
	{
		reportParameters.add(reportEmailParameterEntity);

	}

	public void setReportTemplateIdentifier(Enum<?> reportIdentifier)
	{
		this.reportIdentifier = reportIdentifier.toString();

	}

	@Override
	public Date getNextScheduledTime()
	{
		return nextScheduledTime;
	}

	@Override
	public void setNextScheduledRunTime(Date nextRuntime)
	{
		nextScheduledTime = nextRuntime;

	}

	@Override
	public OutputFormat getOutputFormat()
	{
		return outputFormat;
	}

	public void setOutputFormat(OutputFormat format)
	{
		outputFormat = format;

	}

	/**
	 * @return the reportLog
	 */
	public String getReportLog()
	{
		return reportLog;
	}

}
