package au.com.vaadinutils.wizards.bulkJasperEmail;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.Transaction;
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

import com.vaadin.ui.Notification.Type;

public class SendEmailTask extends ProgressBarTask<JasperTransmission> implements CancelListener,
		JasperReportProperties
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
	public void run()
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

	private void sendMessages(List<JasperTransmission> targets, JasperProxy proxy)

	{

		int sent = 0;
		Transaction t = new Transaction(EntityManagerProvider.createEntityManager());
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
					super.taskItemError(transmission);
					VUNotification.show(e, Type.ERROR_MESSAGE);
				}

				// try
				// {
				// // daoSMTPSettings.sendEmail(settings,
				// proxy.getSenderEmailAddress(), proxy.getRecipient(),
				// // null, proxy.getSubject(), proxy.getBody(),
				// proxy.getAttachments());
				//
				// // Log the activity
				// ActivityDao daoActivity = new DaoFactory().getActivityDao();
				// ActivityTypeDao daoActivityType = new
				// DaoFactory().getActivityTypeDao();
				// ActivityType type =
				// daoActivityType.findByName(ActivityType.BULK_EMAIL);
				// Activity activity = new Activity();
				// activity.setAddedBy(user);
				// //activity.setWithContact(transmission.getContact());
				// activity.setSubject(proxy.getSubject());
				// activity.setDetails(proxy.getBody());
				// activity.setType(type);
				//
				// daoActivity.persist(activity);
				// sent++;
				// super.taskProgress(sent, targets.size(), transmission);
				// SMNotification.show("Email sent to " +
				// transmission.getDescription(), Type.TRAY_NOTIFICATION);
				//
				// // SMNotification.show("Message sent",
				// // Type.TRAY_NOTIFICATION);
				// }
				// catch (EmailException e)
				// {
				// logger.error(e, e);
				// transmission.setException(e);
				// super.taskItemError(transmission);
				// SMNotification.show(e, Type.ERROR_MESSAGE);
				// }
			}

			t.commit();
		}
		catch (Throwable e)
		{
			logger.error(e, e);
			VUNotification.show(e, Type.ERROR_MESSAGE);

		}
		finally
		{

			t.close();
			super.taskComplete(sent);
		}
	}

	@Override
	public void cancel()
	{
		this.cancel = true;

	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params,String reportFilename, 
			CleanupCallback cleanupCallback) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void closeDBConnection()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void initDBConnection()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public OutputFormat getDefaultFormat()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CleanupCallback getCleanupCallback()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDynamicHeaderImage(int pageWidth, String reportTitle)
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReportTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeaderFooterTemplateName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsername()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection getConnection()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getReportFolder()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends JasperReportProperties> getReportClass()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserEmailAddress()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enum<?> getReportIdentifier()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
