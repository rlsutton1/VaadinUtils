package au.com.vaadinutils.listener;

import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.FormHelper;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

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
				if (e instanceof ConstraintViolationException)
				{
					FormHelper.showConstraintViolation((ConstraintViolationException) e);
				}
				logger.error(e, e);
				if (e.getMessage() != null)
				{
					String msg = e.getMessage();
					final String runtimeMessage = "java.lang.RuntimeException: ";
					if (msg.startsWith(runtimeMessage))
					{
						msg = msg.substring(runtimeMessage.length());
					}
						
					Notification.show(msg, Type.ERROR_MESSAGE);

				}
				else
				{
					Notification.show(e.getClass().getSimpleName() + " " + e.getMessage(), Type.ERROR_MESSAGE);
				}
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