package au.com.vaadinutils.listener;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public abstract class ClickListenerLogged implements ClickListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7420365324169589382L;

	Logger logger = Logger.getLogger(ClickListenerLogged.class);

	abstract public void clicked(ClickEvent event);

	@Override
	public void buttonClick(ClickEvent event)
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
