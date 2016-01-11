package au.com.vaadinutils.dao;

import java.util.concurrent.Callable;

/**
 * Allows a Callable to be wrapped in an EM.
 *
 * Usage:
 *
 * @formatter:off
 *
 * 	emc = new EntityManagerCallable(new Callable<T>() { T call() {return someT } };
 *  ThreadPoolExecutor executor = (ThreadPoolExecutor)
 *  Executors.newFixedThreadPool(1); executor.submit(emc);
 *
 * @formatter:on
 *
 * @author bsutton
 *
 * @param <T>
 */
final public class EntityManagerCallable<T> implements Callable<T>
{

	/**
	 * Wraps the passed Callable with the necessary instrumentation to ensure
	 * that an EM is available.
	 */
	private final Callable<T> wrapper;

	public EntityManagerCallable(Callable<T> callable)
	{
		wrapper = EntityManagerProvider.setThreadLocalEntityManager(callable);

	}

	public EntityManagerCallable(CallableUI<T> callable)
	{
		this.wrapper = EntityManagerProvider.setThreadLocalEntityManager(callable);
	}

	@Override
	public T call() throws Exception
	{
		return wrapper.call();
	}

}
