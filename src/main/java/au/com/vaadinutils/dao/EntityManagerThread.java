package au.com.vaadinutils.dao;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Creates a Thread that has its own private EntityManager attached.
 *
 * This class should be used if you need to access a Dao object in a background
 * thread that doesn't have an entity manager injected. This class injects the
 * em into the thread.
 *
 * @author bsutton
 *
 */
public class EntityManagerThread<T>
{
	private final Future<T> future;

	/**
	 * Create an thread with a copy of the current threads UI (because you can't
	 * get the UI from within the new thread), inject an entity manager and then
	 * executes the callable on the new thread.
	 *
	 * The callable can optionally return a result of type T which can be
	 * retrieved by calling get().
	 *
	 * @param ui
	 * @param callable
	 */
	public EntityManagerThread(final UICallable<T> callable)
	{

		this((Callable<T>) callable);

	}

	/**
	 * Injects an entity manager and then runs your callable.
	 *
	 * The callable can optionally return a result of type T which can be
	 * retrieved by calling get().
	 *
	 * @param ui
	 * @param callable
	 */

	public EntityManagerThread(final Callable<T> callable)
	{

		ExecutorService executor = Executors.newFixedThreadPool(1);

		Callable<T> thread = new Callable<T>()
		{

			@Override
			public T call() throws Exception
			{

				return EntityManagerProvider.setThreadLocalEntityManager(new EntityWorker<T>()
				{

					@Override
					public T exec() throws Exception
					{
						return callable.call();
					}

				});

			}

		};

		future = executor.submit(thread);
		executor.shutdown();

	}

	/**
	 * Waits until the thread completes and returns the results of the thread.
	 *
	 * If the thread throws an exception calling this method will result in the
	 * original exception being re-thrown so you can catch it and do something
	 * useful with it.
	 *
	 * @return the results of the callable.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public T get() throws InterruptedException, ExecutionException
	{
		return this.future.get();
	}

}
