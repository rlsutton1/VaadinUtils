package au.com.vaadinutils.jasper.scheduler;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;

import org.apache.commons.mail.EmailException;

import au.com.vaadinutils.jasper.AttachmentType;
import au.com.vaadinutils.jasper.JasperEmailBuilder;
import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.RenderedReport;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.parameter.ReportParameterConstant;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailRecipient;
import au.com.vaadinutils.jasper.ui.CleanupCallback;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;
import au.com.vaadinutils.jasper.ui.JasperReportPropertiesAlternateFile;

import com.google.common.base.Preconditions;

public class ReportEmailRunnerImpl implements ReportEmailRunner, JasperReportProperties
{
	private JasperReportProperties jasperReportProperties;
	private ReportEmailSchedule schedule;

	// Logger logger = LogManager.getLogger();

	@Override
	public boolean runReport(ReportEmailSchedule schedule, Date scheduledTime, JasperEmailSettings emailSettings)
			throws InterruptedException, IOException, EmailException, InstantiationException, IllegalAccessException,
			AddressException, ClassNotFoundException
	{
		Preconditions.checkNotNull(schedule.getSendersEmailAddress(),"Missing senders email address.");
		Preconditions.checkNotNull(schedule.getRecipients(),"Missing recipient email address");
		Preconditions.checkArgument(schedule.getRecipients().size()>0,"Missing recipient email address");
		
		Class<? extends JasperReportProperties> jrpClass = schedule.getJasperReportPropertiesClass();

		jasperReportProperties = jrpClass.newInstance();
		this.schedule = schedule;

		jasperReportProperties = new JasperReportPropertiesAlternateFile(schedule.getReportTitle(),schedule.getReportFileName(), jasperReportProperties);
		Collection<ReportParameter<?>> params = buildParams(schedule, scheduledTime, this);

		JasperManager manager = new JasperManager(this);
		if (manager.checkQueueSize() > 0)
		{
			return false;
		}

		JasperEmailBuilder builder = new JasperEmailBuilder(emailSettings);
		OutputFormat outputFormat = schedule.getOutputFormat();
		RenderedReport export = manager.export(outputFormat, params);
		try
		{
			AttachmentType attachementType = outputFormat.getAttachementType();
			builder.setFrom(schedule.getSendersEmailAddress().toString()).setSubject(schedule.subject())

			.setHtmlBody(schedule.message())
					.addAttachement(export.getBodyAsDataSource(schedule.getReportTitle()+attachementType.getFileExtension(), attachementType));

			for (ReportEmailRecipient address : schedule.getRecipients())
			{
				switch (address.getVisibility())
				{
				case TO:
					builder.addTo(address.getEmail());
					break;
				case CC:
					builder.addCC(address.getEmail());
					break;
				case BCC:
					builder.addBCC(address.getEmail());
					break;
				}

			}

			builder.send(false);
		}
		finally
		{
			export.close();
		}

		return true;

	}

	private Collection<ReportParameter<?>> buildParams(ReportEmailSchedule schedule, Date scheduledTime,
			JasperReportProperties jasperReportProperties)
	{

		Collection<ReportParameter<?>> params = new HashSet<ReportParameter<?>>();

		for (ReportEmailParameter param : schedule.getReportParameters())
		{
			ReportParameterConstant<String> parameter = new ReportParameterConstant<String>(param.getName(),
					param.getValue(), param.getLabel(), param.getDisplayValue());

			params.add(parameter);
		}

		// set modified date params
		for (ScheduledDateParameter param : schedule.getDateParameters())
		{
			String start = param.getOffsetType().convertStartDate(param.getStartDate(), scheduledTime, param.getType());
			params.add(new ReportParameterConstant<String>(param.getStartName(), start, param.getLabel()+" From", start));
			
			String end = param.getOffsetType().convertEndDate(param.getEndDate(), scheduledTime, param.getType());
			params.add(new ReportParameterConstant<String>(param.getEndName(), end,param.getLabel()+" To",end));

		}

		return params;
	}
	
	@Override
	public Map<String, Object> getCustomReportParameterMap()
	{
		return jasperReportProperties.getCustomReportParameterMap();
	}


	@Override
	public String getReportTitle()
	{
		return jasperReportProperties.getReportTitle();
	}

	@Override
	public String getReportFileName()
	{
		return jasperReportProperties.getReportFileName();
	}

	@Override
	public File getReportFolder()
	{
		return jasperReportProperties.getReportFolder();
	}

	@Override
	public String getHeaderFooterTemplateName()
	{
		return jasperReportProperties.getHeaderFooterTemplateName();
	}

	@Override
	public String getUsername()
	{
		return schedule.getSendersUsername();
	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder()
	{
		return jasperReportProperties.getFilterBuilder();
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFileName,
			CleanupCallback cleanupCallback) throws Exception
	{
		return jasperReportProperties.prepareData(params, reportFileName, cleanupCallback);
	}

	@Override
	public CleanupCallback getCleanupCallback()
	{
		return jasperReportProperties.getCleanupCallback();
	}

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{
		jasperReportProperties.prepareForOutputFormat(outputFormat);

	}

	@Override
	public void closeDBConnection()
	{
		jasperReportProperties.closeDBConnection();

	}

	@Override
	public void initDBConnection()
	{
		jasperReportProperties.initDBConnection();

	}

	@Override
	public Connection getConnection()
	{
		return jasperReportProperties.getConnection();
	}

	@Override
	public OutputFormat getDefaultFormat()
	{
		return jasperReportProperties.getDefaultFormat();
	}

	@Override
	public String generateDynamicHeaderImage(int pageWidth,int height, String reportTitle)
	{
		return jasperReportProperties.generateDynamicHeaderImage(pageWidth,height, reportTitle);
	}

	@Override
	public boolean isDevMode()
	{
		return jasperReportProperties.isDevMode();
	}

	@Override
	public Class<? extends JasperReportProperties> getReportClass()
	{
		return jasperReportProperties.getReportClass();
	}

	@Override
	public String getUserEmailAddress()
	{
		try
		{
			return schedule.getSendersEmailAddress().toString();
		}
		catch (AddressException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Enum<?> getReportIdentifier()
	{
		return jasperReportProperties.getReportIdentifier();
	}
	

	@Override
	public String getDynamicJrxmlFileName()
	{
		return null;
	}
}
