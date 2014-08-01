package au.com.vaadinutils.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class VUNotification extends Notification
{
	private static final long serialVersionUID = 1L;
	static Logger logger = LogManager.getLogger();

	public VUNotification(final String caption)
	{
		super(caption);
	}

	private static UI getUI()
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
		return ui;
	}

	public static void show(final String caption, final Type type)
	{

		getUI().access(new Runnable()
		{
			@Override
			public void run()
			{
				Notification notification = new Notification(caption, type);
				if (type == Type.TRAY_NOTIFICATION)
					notification.setPosition(Position.BOTTOM_LEFT);
				notification.show(Page.getCurrent());
			}
		});

	}

	public static void show(final String caption, final String description, final Type type)
	{
		getUI().access(new Runnable()
		{
			@Override
			public void run()
			{

				Notification notification = new Notification(caption, description, type);
				if (type == Type.TRAY_NOTIFICATION)
					notification.setPosition(Position.BOTTOM_LEFT);
				notification.show(Page.getCurrent());
			}
		});
	}

	public static void show(final Exception e, final Type type)
	{
		getUI().access(new Runnable()
		{
			@Override
			public void run()
			{

				// find root cause.
				Throwable rootCause = e;

				while (rootCause.getCause() != null)
					rootCause = rootCause.getCause();

				show(rootCause.getClass().getSimpleName() + ":" + rootCause.getMessage(), type);
			}
		});
	}

	public static void show(final Throwable e, final Type type)
	{
		getUI().access(new Runnable()
		{
			@Override
			public void run()
			{

				// find root cause.
				Throwable rootCause = e;

				while (rootCause.getCause() != null)
					rootCause = rootCause.getCause();

				show(rootCause.getClass().getSimpleName() + ":" + rootCause.getMessage(), type);
			}
		});
	}

}
