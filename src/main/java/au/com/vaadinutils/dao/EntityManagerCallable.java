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

	@Override
	public T call()
	{
		T result = null;
		try
		{
			result = EntityManagerProvider.setThreadLocalEntityManager(new EntityWorker<T>()
			{

				@Override
				public T exec() throws Exception
				{
					return run(ui);
				}

			});
		}
		catch (Exception e)
		{

			logger.error(e, e);
		}
		return result;

	}

	protected abstract T run(UI ui);

}
