package au.com.vaadinutils.wizards.bulkJasperEmail;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import au.com.vaadinutils.dao.CallableUI;
import au.com.vaadinutils.dao.EntityManagerThread;
import au.com.vaadinutils.jasper.AttachmentType;
import au.com.vaadinutils.jasper.JasperEmailBuilder;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.RenderedReport;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.ui.CleanupCallback;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;
import au.com.vaadinutils.listener.CancelListener;
import au.com.vaadinutils.util.ProgressBarTask;
import au.com.vaadinutils.util.ProgressTaskListener;
import au.com.vaadinutils.util.VUNotification;

public class SendEmailTask extends ProgressBarTask<JasperTransmission> implements CancelListener, JasperReportProperties
{
	transient Logger logger = LogManager.getLogger(SendEmailTask.class);
	private JasperProxy proxy;
	private List<JasperTransmission> transmissions;
	private boolean cancel = false;

	public SendEmailTask(ProgressTaskListener<JasperTransmission> listener, JasperProxy proxy,
			ArrayList<JasperTransmission> transmissions)
	{
		super(listener);
		this.proxy = proxy;
		this.transmissions = transmissions;

	}

	@Override
	public void runUI(UI ui)
	{
		try
		{
			sendMessages(transmissions, proxy);
		}
		catch (Exception e)
		{
			logger.error(e, e);
			super.taskException(e);
		}

	}

	private void sendMessages(final List<JasperTransmission> targets, final JasperProxy proxy) 

	{

		new EntityManagerThread<Void>(new CallableUI<Void>(UI.getCurrent())
		{

			@Override
			protected Void call(UI ui) throws Exception
			{
				int sent = 0;
				try
				{
					for (JasperTransmission transmission : targets)
					{
						if (cancel == true)
							break;

						try
						{
							JasperManager manager = proxy.getManager();
							RenderedReport renderedHtml = manager.export(OutputFormat.HTML, null);
							RenderedReport renderedPDF = manager.export(OutputFormat.PDF, null);
							JasperEmailBuilder builder = new JasperEmailBuilder(proxy.getEmailSettings());
							builder.setFrom(proxy.getSenderEmailAddress()).setSubject(proxy.getSubject())
									// .setHtmlBody("<html><body></body></html>")
									.setHtmlBody(renderedHtml).addTo(transmission.getRecipientEmailAddress())
									.addAttachement(renderedPDF.getBodyAsDataSource("report.pdf", AttachmentType.PDF));

							builder.send(false);
						}
						catch (EmailException e)
						{
							logger.error(e, e);
							transmission.setException(e);
							SendEmailTask.super.taskItemError(transmission);
							VUNotification.show(e, Type.ERROR_MESSAGE);
						}

					}

				}
				catch (Exception e)
				{
					VUNotification.show(e, Type.ERROR_MESSAGE);
					throw e;

				}
				finally
				{

					SendEmailTask.super.taskComplete(sent);
				}
				return null;

			}
		});

	}

	@Override
	public void cancel()
	{
		this.cancel = true;

	}

	@Override
	public Map<String, Object> getCustomReportParameterMap()
	{
		return null;

	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder()
	{

		return null;
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFilename,
			CleanupCallback cleanupCallback) throws Exception
	{

		return null;
	}

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{

	}

	@Override
	public void closeDBConnection()
	{

	}

	@Override
	public void initDBConnection()
	{

	}

	@Override
	public OutputFormat getDefaultFormat()
	{

		return null;
	}

	@Override
	public CleanupCallback getCleanupCallback()
	{

		return null;
	}

	@Override
	public String generateDynamicHeaderImage(int pageWidth, int height, String reportTitle)
	{

		return null;
	}

	@Override
	public boolean isDevMode()
	{
		return false;
	}

	@Override
	public String getReportFileName()
	{

		return null;
	}

	@Override
	public String getReportTitle()
	{

		return null;
	}

	@Override
	public String getHeaderFooterTemplateName()
	{

		return null;
	}

	@Override
	public String getUsername()
	{

		return null;
	}

	@Override
	public Connection getConnection()
	{

		return null;
	}

	@Override
	public File getReportFolder()
	{

		return null;
	}

	@Override
	public Class<? extends JasperReportProperties> getReportClass()
	{

		return null;
	}

	@Override
	public String getUserEmailAddress()
	{

		return null;
	}

	@Override
	public Enum<?> getReportIdentifier()
	{

		return null;
	}

	@Override
	public String getDynamicJrxmlFileName()
	{
		return null;
	}
}
