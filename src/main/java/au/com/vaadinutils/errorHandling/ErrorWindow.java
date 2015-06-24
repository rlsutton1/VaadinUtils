package au.com.vaadinutils.errorHandling;

import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class ErrorWindow
{
	static Logger logger = LogManager.getLogger();

	public ErrorWindow()
	{
		// Configure the error handler for the UI
		UI.getCurrent().setErrorHandler(new DefaultErrorHandler()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void error(com.vaadin.server.ErrorEvent event)
			{
				showErrorWindow(event.getThrowable());
			}

		});
	}

	static public void showErrorWindow(Throwable error)
	{

		try
		{
			ViolationConstraintHandler.handleConstraintViolationException(error);
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
			id = "" + id.hashCode();
		}

		final String finalId = id;
		final String finalTrace = fullTrace;
		final Throwable finalCause = cause;
		final String reference = UUID.randomUUID().toString();

		logger.error("Reference: " + reference + " " + error, error);
		logger.error("Reference: " + reference + " " + cause, cause);

		if (UI.getCurrent() != null)
		{
			displayVaadinErrorWindow(causeClass, id, time, finalId, finalTrace, reference);
		}
	}

	private static void displayVaadinErrorWindow(String causeClass, String id, final Date time, final String finalId,
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
		final String supportEmail = ErrorSettingsFactory.getErrorSettings().getTargetEmailAddress();

		Button close = new Button("OK");
		close.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				logger.error("Reference: " + reference + " " + notes.getValue());

				String subject = "";
				String companyName = ErrorSettingsFactory.getErrorSettings().getSystemName();
				subject += "Error: " + finalId + " " + companyName + " ref: " + reference;

				String viewClass = ErrorSettingsFactory.getErrorSettings().getViewName();

				ErrorSettingsFactory.getErrorSettings().sendEmail(
						supportEmail,
						subject,
						subject + "\n\nTime: " + time.toString() + "\n\nView: " + viewClass + "\n\nUser: "
								+ ErrorSettingsFactory.getErrorSettings().getUserName() + "\n\nUser notes:"
								+ notes.getValue() + "\n\n" + finalTrace);
				window.close();

			}
		});
		close.setStyleName(ValoTheme.BUTTON_DANGER);

		Label printMessage = new Label("<font color='red'>Taking a screen shot and sending it to " + supportEmail
				+ " will help with diagnosing the problem</font>");
		printMessage.setContentMode(ContentMode.HTML);

		layout.addComponent(message);
		layout.addComponent(describe);
		layout.addComponent(notes);
		layout.addComponent(printMessage);
		layout.addComponent(close);
		layout.addComponent(new Label("Information about this error will be sent to " + ErrorSettingsFactory.getErrorSettings().getSupportCompanyName()));
		window.setContent(layout);
		// Display the error message in a custom fashion

		// Do the default error handling (optional)
		// doDefault(event);
	}

}
