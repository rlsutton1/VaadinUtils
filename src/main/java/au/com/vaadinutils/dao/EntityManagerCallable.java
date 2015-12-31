package au.com.vaadinutils.dao;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.UI;

/**
 * Designed to work with the EntityManagerThread
 *
 * @author bsutton
 *
 * @param <T>
 */
public abstract class EntityManagerCallable<T> implements Callable<T>
{

	final private UI ui;
	Logger logger = LogManager.getLogger();

	public EntityManagerCallable(UI ui)
	{
		this.ui = ui;
	}

	/**
	 * throws Exception allows the call method to throw an exception. 
	 * The exception is chained from any exception thrown in the enclosed thread.
	 */
	@Override
	public T call() throws Exception
	{

		return run(ui);

	}

	protected abstract T run(UI ui) throws Exception;

}
