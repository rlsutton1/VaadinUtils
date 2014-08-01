package au.com.vaadinutils.wizards.bulkJasperEmail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.sf.jasperreports.engine.JRException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.teemu.wizards.WizardStep;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.fields.PoJoTable;
import au.com.vaadinutils.ui.WorkingDialog;
import au.com.vaadinutils.util.MutableInteger;
import au.com.vaadinutils.util.ProgressBarWorker;
import au.com.vaadinutils.util.ProgressTaskListener;
import au.com.vaadinutils.util.VUNotification;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ShowProgressStep<C extends CrudEntity> implements WizardStep, ProgressTaskListener<JasperTransmission>
{
	static private transient Logger logger = LogManager.getLogger(ShowProgressStep.class);
	JPAContainer<C> entities;
	private WizardView<?, ?, ?> wizardView;
	private boolean sendComplete = false;
	private ProgressBar indicator;
	private Label progressDescription;
	private PoJoTable<JasperTransmission> progressTable;
	private MutableInteger queued = new MutableInteger(0);
	private MutableInteger rejected = new MutableInteger(0);
	private WorkingDialog workDialog;

	public ShowProgressStep(WizardView<?, ?, ?> wizardView)
	{
		this.wizardView = wizardView;
	}

	@Override
	public String getCaption()
	{
		return "Send Messages";
	}

	@Override
	public Component getContent()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		progressTable = new PoJoTable<JasperTransmission>(JasperTransmission.class, new String[] { "Description",
				"RecipientEmailAddress", "Exception" });
		progressTable.setColumnWidth("Description", 80);
		progressTable.setColumnWidth("RecipientEmailAddress", 100);
		progressTable.setColumnExpandRatio("Exception", 1);
		progressTable.setSizeFull();
		progressDescription = new Label();
		layout.addComponent(progressDescription);
		layout.setMargin(true);
		indicator = new ProgressBar(new Float(0.0));
		indicator.setHeight("30px");
		indicator.setIndeterminate(false);
		indicator.setImmediate(true);
		indicator.setSizeFull();
		layout.addComponent(indicator);
		layout.addComponent(this.progressTable);
		layout.setExpandRatio(progressTable, 1);

		Collection<?> recipients = wizardView.getRecipientStep().getRecipientIds();

		ArrayList<JasperTransmission> transmissions = new ArrayList<JasperTransmission>();

		HashSet<String> dedupList = new HashSet<String>();
		JasperProxy proxy;
		try
		{
			proxy = wizardView.getJasperProxy();
		}
		catch (JRException e)
		{
			logger.error(e, e);
			VUNotification.show(e, Type.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}

		for (Object recipientId : recipients)
		{
			Recipient recipient = this.wizardView.getRecipient((Long) recipientId);

			// Find if the recipient has ane email address
			String email = recipient.getEmailAddress();
			if (email != null && email.length() > 0)
			{
				queueTransmission(transmissions, dedupList, recipient, email, proxy);
				continue;
			}

			// No email address found
			JasperTransmission transmission = new JasperTransmission(recipient, proxy, new RecipientException(
					"No email address on recipient.", recipient));
			ShowProgressStep.this.progressTable.addRow(transmission);
			rejected.setValue(rejected.intValue() + 1);
		}

		if (transmissions.size() == 0)
		{
			Notification.show("None of the selected recipients have an Email address", Type.ERROR_MESSAGE);
		}
		else
		{
			queued.setValue(transmissions.size());
			progressDescription.setValue(queued.intValue() + " messages queued.");

			SendEmailTask task = new SendEmailTask(this, proxy, transmissions);

			workDialog = new WorkingDialog("Sending Emails", "Sending...", task);

			ProgressBarWorker<JasperTransmission> worker = new ProgressBarWorker<JasperTransmission>(task);
			worker.start();

			UI.getCurrent().addWindow(workDialog);

		}

		return layout;
	}

	private void queueTransmission(ArrayList<JasperTransmission> transmissions, HashSet<String> dedupList,
			Recipient recipient, String toEmailAddress, JasperProxy proxy)
	{
		JasperTransmission transmission = new JasperTransmission(recipient);
		if (!dedupList.contains(toEmailAddress))
		{
			dedupList.add(toEmailAddress);
			transmissions.add(transmission);
		}
		else
		{
			transmission.setException(new RecipientException("Duplicate email address.", recipient));
			ShowProgressStep.this.progressTable.addRow(transmission);
		}
	}

	@Override
	public boolean onAdvance()
	{
		return sendComplete;
	}

	@Override
	public boolean onBack()
	{
		return true;
	}

	/**
	 * you better get a lock on the UI before calling this method!
	 */
	public final void taskProgress(final int count, final int max, final JasperTransmission status)
	{

		UI ui = UI.getCurrent();
		if (ui == null)
		{
			throw new RuntimeException("You appear to be calling from a worker thread, no UI is available");
		}
		if (!ui.isAttached())
		{
			logger.warn("The UI is nolonger attached, cant deliver message to user");
		}

		String message = "Sending: " + count + " of " + max + " messages.";
		progressDescription.setValue(message);
		indicator.setValue((float) count / max);
		workDialog.progress(count, max, message);
		ShowProgressStep.this.progressTable.addRow(status);

	}

	public final void taskComplete(final int sent)
	{
		UI ui = UI.getCurrent();
		if (ui == null)
		{
			throw new RuntimeException("You appear to be calling from a worker thread, no UI is available");
		}
		if (!ui.isAttached())
		{
			logger.warn("The UI is nolonger attached, cant deliver message to user");
		}
		sendComplete = true;
		indicator.setValue(1.0f);

		if (ShowProgressStep.this.rejected.intValue() == 0 && queued.intValue() == sent)
			progressDescription.setValue("All Email Messages have been sent successfully.");

		else
			progressDescription.setValue(sent + " Email Message " + (sent == 1 ? "has" : "s have")
					+ " been sent successfully. Check the list below for the reason why some of the messages failed.");
		VUNotification.show("Email batch send complete", Type.TRAY_NOTIFICATION);
		workDialog.complete(sent);

	}

	@Override
	public void taskItemError(JasperTransmission transmission)
	{
		this.progressTable.addRow(transmission);

	}

	@Override
	public void taskException(Exception e)
	{
		Notification.show("Error occurred sending Message.", e.getMessage(), Type.ERROR_MESSAGE);

	}
}
