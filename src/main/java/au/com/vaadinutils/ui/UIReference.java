package au.com.vaadinutils.ui;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * The purpose of this class is to hold a reference to NavigatorUI without
 * causing memory leaks.
 * 
 * A single leaked NavigatorUI can easily be > 20MBs
 * 
 * It logs attempts to access the NavigatorUI when it is detached.
 * 
 * It nulls out it's internal reference to the NavigatorUI when it becomes
 * detached.
 * 
 * @author rsutton
 *
 */
public class UIReference implements DetachListener
{
	private static final long serialVersionUID = 1L;
	private final WeakReference<UI> uiRef;
	private final static Logger logger = LogManager.getLogger();
	private Expiry expiry;

	public UIReference()
	{
		UI ui = UI.getCurrent();
		if (ui == null)
		{
			throw new RuntimeException("UI is not available");
		}
		ui.addDetachListener(this);
		this.uiRef = new WeakReference<UI>(ui);

	}

	@Override
	public void detach(DetachEvent event)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.removeDetachListener(this);
		}
		uiRef.clear();
		expiry = new Expiry(TimeUnit.SECONDS, 5);
	}

	public void access(Runnable runnable)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.access(runnable);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public boolean isAttached()
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			return ui.isAttached();
		}
		return false;
	}

	public void addWindow(Window window)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.addWindow(window);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public void removePollListener(PollListener pollListener)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.removePollListener(pollListener);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public void addPollListener(PollListener pollListener)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.addPollListener(pollListener);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public void addDetachListener(DetachListener detachListener)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.addDetachListener(detachListener);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public void setPollInterval(int b)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.setPollInterval(b);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public void removeWindow(Window window)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.removeWindow(window);
		}
		else
		{
			logDetachedAccess();
		}
	}

	public UI getUI()
	{
		return uiRef.get();
	}

	void logDetachedAccess()
	{
		if (expiry != null && expiry.isExpired())
		{
			Exception e = new Exception("Attempt to access detached UI");
			logger.error(e, e);
		}
		else
		{
			logger.warn("Attempt to access expired UI");
		}
	}

	public Navigator getNavigator()
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			return ui.getNavigator();
		}
		else
		{
			logDetachedAccess();
		}
		return null;

	}

	public void removeDetachListener(DetachListener detachHelper)
	{
		UI ui = uiRef.get();
		if (ui != null)
		{
			ui.removeDetachListener(detachHelper);
		}
		else
		{
			logDetachedAccess();
		}

	}

}
