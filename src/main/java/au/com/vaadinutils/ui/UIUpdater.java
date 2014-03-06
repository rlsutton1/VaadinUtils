package au.com.vaadinutils.ui;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

/**
 * This class is designed to get around a bug with the Vaadin access method which doesn't seem
 * to lock the session correctly causing the UI to freeze if you attempt to updated it from 
 * a background thread.
 * 
 * 
 * @author bsutton
 *
 */
public class UIUpdater
{
	private static Logger logger = LogManager.getLogger(UIUpdater.class);

	public UIUpdater(final Runnable uiRunnable)
	{
		UI.getCurrent().access(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					VaadinSession.getCurrent().getLockInstance().lock();
					uiRunnable.run();
				}
				catch(Throwable e)
				{
					logger.error(e,e);
					Notification.show(e.getClass().getSimpleName() + ":" + e.getMessage(), Type.ERROR_MESSAGE);
				}
				finally
				{
					VaadinSession.getCurrent().getLockInstance().unlock();
				}

			}
		});

	}

}
