package au.com.vaadinutils.errorHandling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.screenshot.Screenshot;
import org.vaadin.addons.screenshot.ScreenshotImage;
import org.vaadin.addons.screenshot.ScreenshotListener;
import org.vaadin.addons.screenshot.ScreenshotMimeType;

import com.google.common.base.Stopwatch;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class ErrorWindow
{
	private Button close = new Button("OK");
	private Label uploadStatus = new Label("&nbsp;", ContentMode.HTML);

	static Logger logger = LogManager.getLogger();

	/**
	 * throttle for sending emails about errors the user hasn't seen. Allow
	 * busting to 20 emails in a minute, over the long term limit to 1 email per
	 * minute
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
				new ErrorWindow(true).internalShowErrorWindow(event.getThrowable());
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

	static final ThreadLocal<String> lastSeenError = new ThreadLocal<>();

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

		if (lastSeenError.get() != null && lastSeenError.get().equals(id))
		{
			logger.error("Skipping repeated error " + error.getMessage());
			return;
		}

		lastSeenError.set(id);

		final String finalId = id;
		final String finalTrace = fullTrace;
		// final Throwable finalCause = cause;
		final String reference = UUID.randomUUID().toString();

		logger.error("Reference: " + reference + " Version: " + getBuildVersion() + " System: " + getSystemName() + " "
				+ error, error);
		logger.error("Reference: " + reference + " " + cause, cause);

		final String finalCauseClass = causeClass;

		if (!isExempted(cause))
		{
			if (UI.getCurrent() != null)
			{
				UI.getCurrent().access(new Runnable()
				{

					@Override
					public void run()
					{

						Stopwatch lastTime = (Stopwatch) UI.getCurrent().getSession()
								.getAttribute("Last Time Error Window Shown");

						// don't display the error window more than once every 2
						// seconds
						if (lastTime == null || lastTime.elapsed(TimeUnit.SECONDS) > 2)
						{

							displayVaadinErrorWindow(finalCauseClass, finalId, time, finalId, finalTrace, reference);

							UI.getCurrent().getSession().setAttribute("Last Time Error Window Shown",
									Stopwatch.createStarted());
						}
						else
						{
							emailErrorWithoutShowing(time, finalId, finalTrace, reference);
						}
					}
				});
			}
			else
			{

				emailErrorWithoutShowing(time, finalId, finalTrace, reference);
			}
		}
		else
		{
			logger.error("Not Sending email or displaying error as cause is exempted.");
		}
	}

	private void emailErrorWithoutShowing(final Date time, final String finalId, final String finalTrace,
			final String reference)
	{
		// limit the number of errors that can be emailed without human
		// action. also suppress some types of errors
		if (emailRateController.acquire())
		{
			try
			{
				final String supportEmail = getTargetEmailAddress();

				generateEmail(time, finalId, finalTrace, reference, "Error not displayed to user", supportEmail, "", "",
						"", null);
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

	boolean isExempted(Throwable cause)
	{
		Map<String, String> exemptedExceptions = new HashMap<>();
		exemptedExceptions.put("ClientAbortException", "");
		exemptedExceptions.put("SocketException", "");
		exemptedExceptions.put("UIDetachedException", "");
		exemptedExceptions.put("IOException", "Pipe closed");

		String expectedMessage = exemptedExceptions.get(cause.getClass().getSimpleName());
		if (expectedMessage != null)
		{
			if (StringUtils.isNotEmpty(expectedMessage))
			{
				return cause.getMessage().equalsIgnoreCase(expectedMessage);
			}
			return true;
		}
		return false;
	}

	private void displayVaadinErrorWindow(final String causeClass, final String id, final Date time,
			final String finalId, final String finalTrace, final String reference)
	{

		// generate screen shot!

		final Window window = new Window();
		final Screenshot screenshot = Screenshot.newBuilder().withLogging(true).withMimeType(ScreenshotMimeType.PNG)
				.build();
		screenshot.addScreenshotListener(new ScreenshotListener()
		{
			@Override
			public void screenshotComplete(ScreenshotImage image)
			{
				image.getImageData();
				showWindow(causeClass, id, time, finalId, finalTrace, reference, image.getImageData());
				window.close();

			}
		});

		window.setContent(screenshot);
		window.setClosable(false);
		window.setResizable(false);

		UI.getCurrent().addWindow(window);
		screenshot.setTargetComponent(null);
		screenshot.takeScreenshot();

	}

	private void showWindow(String causeClass, String id, final Date time, final String finalId,
			final String finalTrace, final String reference, final byte[] imageData)
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

		final Label message = new Label(
				"<b>An error has occurred (" + causeClass + ").<br><br>Reference:</b> " + reference);
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
							getUserName(), getUserEmail(), imageData);
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

		layout.addComponent(message);
		layout.addComponent(describe);
		layout.addComponent(notes);
		layout.addComponent(uploadStatus);
		layout.addComponent(close);
		layout.addComponent(new Label("Information about this error will be sent to " + getSupportCompanyName()));
		window.setContent(layout);
	}

	private void generateEmail(final Date time, final String finalId, final String finalTrace, final String reference,
			final String notes, final String supportEmail, final String viewClass, final String user,
			final String userEmail, final byte[] imageData)
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

				ByteArrayOutputStream stream = null;
				String filename = null;
				String MIMEType = null;
				if (imageData != null)
				{
					stream = new ByteArrayOutputStream();
					try
					{
						stream.write(imageData);
						filename = "screen.png";
						MIMEType = ScreenshotMimeType.PNG.getMimeType();
					}
					catch (IOException e)
					{
						logger.error(e, e);
					}
				}
				ErrorSettingsFactory.getErrorSettings().sendEmail(supportEmail, subject,
						subject + "\n\nTime: " + time.toString() + "\n\nView: " + viewClass + "\n\nUser: " + user + " "
								+ userEmail + "\n\n" + "Version: " + buildVersion + "\n\n" + "User notes:" + notes
								+ "\n\n" + finalTrace,
						stream, filename, MIMEType);

			}
		};

		new Thread(runner, "Send Error Email").start();
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
