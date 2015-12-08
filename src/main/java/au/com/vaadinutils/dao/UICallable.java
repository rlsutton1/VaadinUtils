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
public abstract class UICallable<T> implements Callable<T>
{

	final private UI ui = UI.getCurrent();
	Logger logger = LogManager.getLogger();

	@Override
	public T call()
	{

		return run(ui);

	}

	protected abstract T run(UI ui);

}
