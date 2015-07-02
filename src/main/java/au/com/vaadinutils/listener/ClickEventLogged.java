package au.com.vaadinutils.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.errorHandling.ErrorWindow;

public class ClickEventLogged
{
	static public abstract class ClickListener implements com.vaadin.ui.Button.ClickListener
	{
		private static final long serialVersionUID = 7420365324169589382L;

		transient Logger logger = LogManager.getLogger(ClickListener.class);

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
				ErrorWindow.showErrorWindow(e);
			}

		}

	}

	static public class ClickAdaptor implements com.vaadin.ui.Button.ClickListener
	{
		private static final long serialVersionUID = 1L;

		transient Logger logger = LogManager.getLogger(ClickAdaptor.class);

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