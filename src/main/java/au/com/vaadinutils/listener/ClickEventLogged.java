package au.com.vaadinutils.listener;

import org.apache.log4j.Logger;

import com.vaadin.ui.Notification.Type;

import com.vaadin.ui.Notification;

public class ClickEventLogged
{
	static public abstract class ClickListener implements com.vaadin.ui.Button.ClickListener
	{
		private static final long serialVersionUID = 7420365324169589382L;

		Logger logger = Logger.getLogger(ClickListener.class);

		abstract public void clicked(com.vaadin.ui.Button.ClickEvent event);

		@Override
		public void buttonClick(com.vaadin.ui.Button.ClickEvent event)
		{
			try
			{
				clicked(event);
			}
			catch (Throwable e)
			{
				logger.error(e, e);
				Notification.show(e.getMessage(),Type.ERROR_MESSAGE);
			}

		}

	}
	
	static public class ClickAdaptor implements com.vaadin.ui.Button.ClickListener
	{
		private static final long serialVersionUID = 1L;

		Logger logger = Logger.getLogger(ClickAdaptor.class);

		private com.vaadin.ui.Button.ClickListener listener = null;

		public ClickAdaptor(com.vaadin.ui.Button.ClickListener listener)
		{
			this.listener = listener;
		}
		public void buttonClick(com.vaadin.ui.Button.ClickEvent event)
		{
			try
			{
				listener.buttonClick(event);
			}
			catch (Throwable e)
			{
				logger.error(e, e);
				throw new RuntimeException(e);
			}

		}
	}

}