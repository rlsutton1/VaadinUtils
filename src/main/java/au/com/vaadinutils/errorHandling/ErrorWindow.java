package au.com.vaadinutils.errorHandling;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class ErrorWindow
{
	private static final Long MAX_ATTACHMENT_SIZE = new Long(2000000); // 2MB
	private Button close = new Button("OK");
	private Label uploadStatus = new Label("&nbsp;", ContentMode.HTML);

	static Logger logger = LogManager.getLogger();

	/**
	 * throttle for sending emails about errors the user hasn't seen.
	 * 
	 * Allow busting to 20 emails in a minute, over the long term limit to 1
	 * email per minute
	 */
	final static ErrorRateController emailRateController = new ErrorRateController(20, 1, TimeUnit.MINUTES);

	public ErrorWindow()
	{
		// Configure the error handler for the UI
		UI.getCurrent().setErrorHandler(new DefaultErrorHandler()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void error(com.vaadin.server.ErrorEvent event)
			{
				internalShowErrorWindow(event.getThrowable());
			}

		});
	}

	ErrorWindow(boolean noUI)
	{

	}

	public static void showErrorWindow(Throwable e)
	{
		new ErrorWindow(true).internalShowErrorWindow(e);

	}

	private void internalShowErrorWindow(Throwable error)
	{

		try
		{
			ViolationConstraintHandler.expandException(error);
		}
		catch (Throwable e)
		{
			error = e;
		}

		// Find the final cause
		String fullTrace = "";

		String causeClass = "";
		String id = "";

		final Date time = new Date();
		Throwable cause = null;
		for (Throwable t = error; t != null; t = t.getCause())
		{
			if (t.getCause() == null) // We're at final cause
				cause = t;
			fullTrace += t.getClass().getCanonicalName() + " " + t.getMessage() + "\n";
			for (StackTraceElement trace : t.getStackTrace())
			{
				fullTrace += "at " + trace.getClassName() + "." + trace.getMethodName() + "(" + trace.getFileName()
						+ ":" + trace.getLineNumber() + ")\n";
			}
			fullTrace += "\n\n";
		}
		if (cause != null)
		{
			causeClass = cause.getClass().getSimpleName();

			id = cause.getClass().getCanonicalName() + "\n";
			for (StackTraceElement trace : cause.getStackTrace())
			{
				id += "at " + trace.getClassName() + "." + trace.getMethodName() + "(" + trace.getFileName() + ":"
						+ trace.getLineNumber() + ")\n";
			}
			// prevent hashcode being negative
			Long hashId = new Long(id.hashCode()) + new Long(Integer.MAX_VALUE);
			id = "" + hashId;

		}

		final String finalId = id;
		final String finalTrace = fullTrace;
		// final Throwable finalCause = cause;
		final String reference = UUID.randomUUID().toString();

		logger.error("Reference: " + reference + " Version: " + getBuildVersion() + " System: " + getSystemName() + " "
				+ error, error);
		logger.error("Reference: " + reference + " " + cause, cause);

		final String finalCauseClass = causeClass;

		if (UI.getCurrent() != null)
		{
			UI.getCurrent().access(new Runnable()
			{

				@Override
				public void run()
				{
					displayVaadinErrorWindow(finalCauseClass, finalId, time, finalId, finalTrace, reference);

				}
			});
		}
		else
		{
			// limit the number of errors that can be emailed without human
			// action. also suppress some types of errors
			if (emailRateController.acquire() && !(cause instanceof SocketException))
			{
				try
				{
					final String supportEmail = getTargetEmailAddress();

					generateEmail(time, finalId, finalTrace, reference, "Error not displayed to user", supportEmail,
							"", "", "");
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
			else
			{
				logger.error("Not sending error email");
			}

		}
	}

	private void displayVaadinErrorWindow(String causeClass, String id, final Date time, final String finalId,
			final String finalTrace, final String reference)
	{
		final Window window = new Window();
		UI.getCurrent().addWindow(window);
		window.setModal(true);
		window.center();
		window.setResizable(false);
		window.setCaption("Error " + id);
		window.setClosable(false);

		// window.setHeight("50%");
		window.setWidth("50%");
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		final Label message = new Label("<b>An error has occurred (" + causeClass + ").<br><br>Reference:</b> "
				+ reference);
		message.setContentMode(ContentMode.HTML);

		Label describe = new Label("<b>Please describe what you were doing when this error occured (Optional)<b>");
		describe.setContentMode(ContentMode.HTML);

		final TextArea notes = new TextArea();
		notes.setWidth("100%");
		final String supportEmail = getTargetEmailAddress();

		close.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				try
				{
					generateEmail(time, finalId, finalTrace, reference, notes.getValue(), supportEmail, getViewName(),
							getUserName(), getUserEmail());
				}
				catch (Exception e)
				{
					logger.error(e, e);
					Notification.show("Error sending error report", Type.ERROR_MESSAGE);
				}
				finally
				{
					window.close();
				}
			}

		});
		close.setStyleName(ValoTheme.BUTTON_DANGER);

		Label printMessage = new Label("<font color='red'>Taking a screen shot and sending it to " + supportEmail
				+ " will help with diagnosing the problem</font>");
		printMessage.setContentMode(ContentMode.HTML);

		layout.addComponent(message);
		layout.addComponent(describe);
		layout.addComponent(notes);
		layout.addComponent(createAttachmentComponent());
		layout.addComponent(uploadStatus);
		layout.addComponent(close);
		layout.addComponent(new Label("Information about this error will be sent to " + getSupportCompanyName()));
		window.setContent(layout);
		// Display the error message in a custom fashion

		// Do the default error handling (optional)
		// doDefault(event);
	}

	private void generateEmail(final Date time, final String finalId, final String finalTrace, final String reference,
			final String notes, final String supportEmail, final String viewClass, final String user,
			final String userEmail)
	{

		logger.error("Reference: " + reference + " " + notes);
		final String buildVersion = getBuildVersion();
		final String companyName = getSystemName();
		Runnable runner = new Runnable()
		{

			@Override
			public void run()
			{
				String subject = "";
				subject += "Error: " + finalId + " " + companyName + " ref: " + reference;

				ErrorSettingsFactory.getErrorSettings().sendEmail(
						supportEmail,
						subject,
						subject + "\n\nTime: " + time.toString() + "\n\nView: " + viewClass + "\n\nUser: " + user + " "
								+ userEmail + "\n\n" + "Version: " + buildVersion + "\n\n" + "User notes:" + notes
								+ "\n\n" + finalTrace, ErrorWindow.this.stream, ErrorWindow.this.filename,
						ErrorWindow.this.MIMEType);

			}
		};

		new Thread(runner, "Send Error Email").start();
	}

	private ByteArrayOutputStream stream = null;
	private String filename;
	private String MIMEType;
	private boolean attachmentTooLarge;

	@SuppressWarnings("serial")
	private Upload createAttachmentComponent()
	{
		final Receiver receiver = new Receiver()
		{

			private static final long serialVersionUID = 3413693084667621411L;

			@Override
			public OutputStream receiveUpload(String filename, String MIMEType)
			{
				ErrorWindow.this.stream = new ByteArrayOutputStream();
				ErrorWindow.this.filename = filename;
				ErrorWindow.this.MIMEType = MIMEType;
				return ErrorWindow.this.stream;
			}
		};

		final Upload upload = new Upload(
				"Taking a screenshot and attaching it will help with diagnosing the problem (Optional - Maximum 2MB)",
				receiver);
		upload.setButtonCaption("Upload Attachment");
		upload.setImmediate(true);

		final SucceededListener succeededListener = new SucceededListener()
		{

			@Override
			public void uploadSucceeded(SucceededEvent event)
			{
				ErrorWindow.this.setUploadStatus("Uploaded attachment: " + event.getFilename(), false);
				close.setEnabled(true);
			}
		};
		upload.addSucceededListener(succeededListener);

		final FailedListener failedListener = new FailedListener()
		{

			@Override
			public void uploadFailed(FailedEvent event)
			{
				if (attachmentTooLarge)
				{
					attachmentTooLarge = false;
					ErrorWindow.this.setUploadStatus("Attachment is too large. Maximum size is 2MB.", true);
				}
				else
				{
					ErrorWindow.this.setUploadStatus("Failed to upload attachment: " + event.getFilename(), true);
					logger.error(event.getReason(), event.getReason());
				}
				close.setEnabled(true);
			}
		};
		upload.addFailedListener(failedListener);

		final StartedListener startedListener = new StartedListener()
		{

			@Override
			public void uploadStarted(StartedEvent event)
			{
				close.setEnabled(false);
				attachmentTooLarge = false;
				if (event.getContentLength() > MAX_ATTACHMENT_SIZE)
				{
					attachmentTooLarge = true;
					upload.interruptUpload();
				}
				else
				{
					ErrorWindow.this.setUploadStatus("Uploading...", false);
				}
			}
		};
		upload.addStartedListener(startedListener);

		return upload;
	}

	private void setUploadStatus(String message, boolean error)
	{
		// Prevent component collapsing
		if (message.isEmpty())
		{
			message = "&nbsp;";
		}
		if (error)
		{
			message = "<font color='red'>" + message + "</font>";
		}
		uploadStatus.setValue(message);
	}

	private String getViewName()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getViewName();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting View name";
	}

	private String getSupportCompanyName()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getSupportCompanyName();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting Support Company Name";
	}

	private String getTargetEmailAddress()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getTargetEmailAddress();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting Target Email Address";
	}

	private String getUserEmail()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getUserEmail();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting user email";
	}

	private String getBuildVersion()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getBuildVersion();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting build Version";
	}

	private String getUserName()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getUserName();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting user name";
	}

	private String getSystemName()
	{
		try
		{
			return ErrorSettingsFactory.getErrorSettings().getSystemName();
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		return "Error getting System name";
	}

}
