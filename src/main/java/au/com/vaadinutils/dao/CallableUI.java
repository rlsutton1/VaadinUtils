package au.com.vaadinutils.dao;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.UI;

/**
 * Designed to work with EntityManagerCallable and EntityManagerThread
 *
 * This method allows you to execute a Callable passing in a Vaadin UI.
 *
 * @author bsutton
 *
 * @param <T>
 */
public abstract class CallableUI<T> implements Callable<T>
{

	final private UI ui;
	Logger logger = LogManager.getLogger();

	public CallableUI(UI ui)
	{
		this.ui = ui;
	}

	/**
	 * throws Exception allows the call method to throw an exception. The
	 * exception is chained from any exception thrown in the enclosed thread.
	 */
	@Override
	public T call() throws Exception
	{

		return call(ui);

	}

	protected abstract T call(UI ui) throws Exception;

}
