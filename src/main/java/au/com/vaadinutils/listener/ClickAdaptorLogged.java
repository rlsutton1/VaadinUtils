package au.com.vaadinutils.listener;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class ClickAdaptorLogged implements ClickListener
{
	private static final long serialVersionUID = 1L;

	Logger logger = Logger.getLogger(ClickAdaptorLogged.class);

	private ClickListener listener = null;

	public ClickAdaptorLogged()
	{

	}

	public ClickAdaptorLogged(ClickListener listener)
	{
		this.listener = listener;
	}

	public void buttonClick(ClickEvent event)
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
