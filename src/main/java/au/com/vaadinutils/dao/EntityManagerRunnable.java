package au.com.vaadinutils.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.UI;

/**
 * Overload this class to create a Runnable that has its own EntityManager and
 * which has access to the vaadin UI object.
 *
 * This class should be used if you need to access a Dao object in a background
 * thread that doesn't have an entity manager injected. This class injects the
 * em into the thread.
 *
 * @author bsutton
 *
 */
public abstract class EntityManagerRunnable implements Runnable
{

	final private UI ui;
	Logger logger = LogManager.getLogger();

	public EntityManagerRunnable(UI ui)
	{
		this.ui = ui;
	}

	@Override
	public void run()
	{
		try
		{
			EntityManagerProvider.setThreadLocalEntityManager(new EntityWorker<Void>()
			{

				@Override
				public Void exec() throws Exception
				{
					run(ui);
					return null;
				}

			});
		}
		catch (Exception e)
		{

			logger.error(e, e);
		}

	}

	public abstract void run(UI ui);

}
