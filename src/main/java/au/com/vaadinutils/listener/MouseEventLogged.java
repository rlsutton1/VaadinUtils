package au.com.vaadinutils.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MouseEventLogged
{
	static public abstract class ClickListener implements com.vaadin.event.MouseEvents.ClickListener
	{
		private static final long serialVersionUID = 7420365324169589382L;

		 transient Logger logger   =  LogManager.getLogger(ClickListener.class);

		abstract public void clicked(com.vaadin.event.MouseEvents.ClickEvent event);

		@Override
		public void click(com.vaadin.event.MouseEvents.ClickEvent event)
		{
			try
			{
				clicked(event);
			}
			catch (Throwable e)
			{
				logger.error(e, e);
				throw new RuntimeException(e);
			}

		}

	}

	static public class ClickAdaptor implements com.vaadin.event.MouseEvents.ClickListener
	{
		private static final long serialVersionUID = 1L;

		 transient Logger logger   =  LogManager.getLogger(ClickAdaptor.class);

		private com.vaadin.event.MouseEvents.ClickListener listener = null;

		public ClickAdaptor(com.vaadin.event.MouseEvents.ClickListener listener)
		{
			this.listener = listener;
		}

		public void click(com.vaadin.event.MouseEvents.ClickEvent event)
		{
			try
			{
				listener.click(event);
			}
			catch (Throwable e)
			{
				logger.error(e, e);
				throw new RuntimeException(e);
			}

		}
	}
}