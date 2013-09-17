package au.com.vaadinutils.wizards.bulkJasperEmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.Transaction;
import au.com.vaadinutils.impl.LocalEntityManagerFactory;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.RenderedReport;
import au.com.vaadinutils.jasper.JasperManager.EmailBuilder;
import au.com.vaadinutils.jasper.JasperManager.Exporter;
import au.com.vaadinutils.listener.CancelListener;
import au.com.vaadinutils.util.ProgressBarTask;
import au.com.vaadinutils.util.ProgressTaskListener;
import au.com.vaadinutils.util.VUNotification;

import com.vaadin.ui.Notification.Type;

public class SendEmailTask extends ProgressBarTask<JasperTransmission> implements CancelListener
{
	Logger logger = Logger.getLogger(SendEmailTask.class);
	private JasperProxy proxy;
	private List<JasperTransmission> transmissions;
	private boolean cancel = false;

	public SendEmailTask(ProgressTaskListener<JasperTransmission> listener,JasperProxy proxy,
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
			throws IOException
	{

		EntityManager em = LocalEntityManagerFactory.createEntityManager();
		int sent = 0;
		Transaction t = new Transaction(em);
		try 
		{
			for (JasperTransmission transmission : targets)
			{
				if (cancel == true)
					break;

				try
				{
					JasperManager manager = proxy.getManager();
					manager.fillReport();
					RenderedReport renderedHtml = manager.export(Exporter.HTML);
					manager.fillReport();
					RenderedReport renderedPDF = manager.export(Exporter.PDF);
					EmailBuilder builder = new JasperManager.EmailBuilder(manager.getSettings());
					builder.setFrom(proxy.getSenderEmailAddress())
					.setSubject(proxy.getSubject())
					//.setHtmlBody("<html><body></body></html>")
					.setHtmlBody(renderedHtml)
					.addTo(transmission.getRecipientEmailAddress())
					.addAttachement(renderedPDF.getBodyAsDataSource());
					
					builder.send();
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
			if (t != null)
				t.close();
			super.taskComplete(sent);

			// Reset the entity manager
			EntityManagerProvider.INSTANCE.setCurrentEntityManager(null);
		}
	}

	@Override
	public void cancel()
	{
		this.cancel = true;

	}

}
