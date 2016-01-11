package au.com.vaadinutils.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pass a Runnable that needs to be wrapped in an EM transaction with its own
 * EM.
 *
 * This class should be used if you need to access a Dao object in a background
 * thread that doesn't have an entity manager injected. This class injects the
 * em into the thread.
 *
 * Usage:
 *
 * @formatter:off
 *
 *  emr = new EntityManagerRunnable( new Runnable () { public void run() { do something with dao };
 *  new Thread(emr).start();
 *
 * @formatter:on
 *
 * @author bsutton
 *
 */
public final class EntityManagerRunnable implements Runnable
{
	private Logger logger = LogManager.getLogger();

	private final Runnable wrapper;

	public EntityManagerRunnable(Runnable runnable)
	{
		wrapper = EntityManagerProvider.setThreadLocalEntityManager(runnable);

	}

	public EntityManagerRunnable(RunnableUI runnable)
	{
		wrapper = EntityManagerProvider.setThreadLocalEntityManager(runnable);

	}

	@Override
	public void run()
	{
		wrapper.run();
	}

}
