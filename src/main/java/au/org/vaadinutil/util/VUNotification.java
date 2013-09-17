package au.org.vaadinutil.util;

import au.com.vaadinutils.ui.UIUpdater;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

public class VUNotification extends Notification
{
	private static final long serialVersionUID = 1L;

	public VUNotification(final String caption)
	{
		super(caption);
	}

	public static void show(final String caption, final Type type)
	{

		new UIUpdater(new Runnable()
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
		new UIUpdater(new Runnable()
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
		new UIUpdater(new Runnable()
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
		new UIUpdater(new Runnable()
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
