package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

import au.com.vaadinutils.errorHandling.ErrorWindow;

/**
 * The class is a place holder to allow access to an 'non-injected' entity
 * manager.
 *
 * Normally you need a EM for each thread and the EntityManagerProvider provides
 * a simple mechanism to inject an EM into a thread.
 *
 * NOTE: You need to initialise this Provider during application startup by
 * calling EntityManagerProvider.setEntityManagerFactory().
 *
 * You shouldn't use this provider directly but rather use it via one of:
 *
 * EntityManagerCallable EntityManagerRunnable EntityManagerThread
 *
 * EntityManagerInjectorFilter AtmosphereFilter
 *
 * Each of these class correctly sets up the EM, starts a Transaction and
 * finally clears the threads EM once the thread is complete.
 *
 * If you want to use EntityManagerProvider directly (DON'T):
 *
 * Then for each thread you need to inject a thread local EntityManager by
 * calling setCurrentEntityManager and then clearing it when the thread shuts
 * down by calling setCurrentEntityManager with a null. You will also need wrap
 * the entity in a Transaction block before calling setCurrentEntityManager.
 *
 * e.g.
 *
 * @formatter:off EntityManager em =
 *                EntityManagerProvider.createEntityManager(); Transaction t =
 *                null; Transaction t = new Transaction(em); try { // Create and
 *                set the entity manager
 *                EntityManagerProvider.setCurrentEntityManager(em); // Handle
 *                the request filterChain.doFilter(servletRequest,
 *                servletResponse);
 *
 *                t.commit(); } finally { if (t!= null) t.close(); // Reset the
 *                entity manager
 *                EntityManagerProvider.setCurrentEntityManager(null); }
 *
 * @formatter:on
 *
 *               You can use the @Link
 *               au.com.vaadinutils.filter.EntityManagerInjectorFilter and @Link
 *               au.com.vaadinutils.filter.AtmosphereFilter to do the injection
 *               or make up your own methods.
 *
 * @author bsutton
 *
 */
public enum EntityManagerProvider
{
	INSTANCE;

	private static final Logger logger = LogManager.getLogger();

	/**
	 * provides a mechanism to register actions that should happen should happen
	 * before a transaction is started and after it is committed.
	 */
	private static List<EMAction> registeredPreActions = new ArrayList<>();

	private static List<Runnable> registeredPostActions = new ArrayList<>();

	private ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();
	private javax.persistence.EntityManagerFactory emf;

	/**
	 * Get the entity manager attached to this thread.
	 *
	 * @return
	 */
	public static EntityManager getEntityManager()
	{
		return INSTANCE.entityManagerThreadLocal.get();
	}

	/**
	 * Set an entity manager for this thread.
	 *
	 * @param em
	 */
	public static void setCurrentEntityManager(EntityManager em)
	{
		EntityManager oldem = INSTANCE.entityManagerThreadLocal.get();

		Preconditions.checkArgument(em == null || (oldem == null && em != null),
				"Can not replace the current entity manager, close it and set it to null first!!!!");

		/**
		 * Before you try to clear or replace the current entity manager you
		 * should do something similar to:
		 *
		 *  @formatter:off
		 *  	try
		 *		{
		 *			transaction.commit();
		 *			transaction.close();
		 *			em.close();
		 *		}
		 *		finally
		 *		{
		 *			EntityManagerProvider.setCurrentEntityManager(null);
		 *		}
		 *  @formatter:on
		 *
		 */
		Preconditions.checkArgument(oldem == null || !oldem.isOpen(),
				"Current entity manager is still open, commit and close any transactions and then close the EntityManager first");

		if (em == null)
		{
			logger.debug("Clearing entity manager for thread {}", Thread.currentThread().getId());
		}
		else
		{
			logger.debug("Setting entity manager for thread {}", Thread.currentThread().getId());
			if (INSTANCE.entityManagerThreadLocal.get() != null)
			{
				logger.error("Setting the entitymanager but the entityManager is already Set.");
			}

		}

		INSTANCE.entityManagerThreadLocal.set(em);

		if (em != null)
		{
			runPreActions(em);
		}
		else
		{
			runPostActions();
		}

	}

	/**
	 * Call this method to initialise the EntityManagerProvider so that it can
	 * hand out EntityManagers to worker threads. Dont forget to close the
	 * entitymanager
	 *
	 * This should normally be called from a servlet Context Listener.
	 *
	 * @param emf
	 */
	public static void setEntityManagerFactory(javax.persistence.EntityManagerFactory emf)
	{
		INSTANCE.emf = emf;
	}

	/**
	 * T return type from EntityWorker.
	 *
	 * @param worker
	 * @return
	 * @throws Exception
	 */
	public static <T> T setThreadLocalEntityManager(EntityWorker<T> worker) throws Exception
	{
		if (getEntityManager() == null)
		{
			final EntityManager em = createEntityManager();

			try
			{
				setCurrentEntityManager(em);

				em.getTransaction().begin();

				T ret = worker.exec();

				em.getTransaction().commit();
				return ret;
			}
			catch (ConstraintViolationException e)
			{
				// ensure we get the cause of an underlying constraint violation
				ErrorWindow.showErrorWindow(e);
				throw e;
			}
			finally
			{
				try
				{
					try
					{
						if (em.getTransaction().isActive())
						{
							logger.error("Rolling back transaction");
							em.getTransaction().rollback();

						}
					}
					finally
					{
						em.close();
					}
				}
				finally
				{
					setCurrentEntityManager(null);
				}
			}
		}
		// there was already an active entity manager, so just use it!
		return worker.exec();

	}

	/**
	 * Allows you to pass a Runnable to wrap in an entity manager.
	 *
	 * A new Runnable is returned which should then be called to run your
	 * runnable.
	 *
	 * i.e. don't run you own runnable directly rather use the returned
	 * Runnable.
	 *
	 * @param runnable
	 *            - the runnable to run as contains an entity manager.
	 * @return
	 */
	public static Runnable setThreadLocalEntityManager(final Runnable runnable)
	{
		return new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					setThreadLocalEntityManager(new EntityWorker<Void>()
					{

						@Override
						public Void exec() throws Exception
						{
							runnable.run();
							return null;
						}
					});
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}

			}
		};
	}

	/**
	 * Allows you to pass in a Callable to wrap in an entity manager.
	 *
	 * A new Callable is returned which should then be called to run your
	 * Callable.
	 *
	 * i.e. don't run you own Callable directly rather use the returned
	 * Callable.
	 *
	 * @param Callable
	 *            - the Callable to run as contains an entity manager.
	 * @return
	 */

	public static <T> Callable<T> setThreadLocalEntityManager(final Callable<T> callable)
	{
		return new Callable<T>()
		{

			@Override
			public T call() throws Exception
			{
				T result = null;
				try
				{
					setThreadLocalEntityManager(new EntityWorker<T>()
					{

						@Override
						public T exec() throws Exception
						{
							return callable.call();
						}
					});

				}
				catch (Exception e)
				{
					logger.error(e, e);
					throw e;
				}
				return result;

			}
		};
	}

	/**
	 *
	 *
	 * If you have a worker thread then it won't have access to a thread local
	 * entity manager (as they are injected by the servlet request filters
	 * mentioned above. <br>
	 * <br>
	 * <b>For worker threads preferably use setThreadLocalEntityManager</b> <br>
	 * <br>
	 * Otherwise you need to call this method to get an entity manager. You will
	 * also need to call close when done
	 *
	 *
	 * @return
	 */
	public static EntityManager createEntityManager()
	{
		if (INSTANCE.emf == null)
		{
			throw new IllegalStateException("Context is not initialized yet.");
		}

		// EntityManager entityManager = new
		// EntityManagerTrackerWrapper(INSTANCE.emf.createEntityManager());
		EntityManager entityManager = INSTANCE.emf.createEntityManager();

		return entityManager;

		// you might want to use this if your having deadlocks...
		// don't ever use JPAFactory to build your JPAContainers
		//return new EntityManagerWrapper(entityManager);
	}

	/**
	 * convienece method
	 *
	 * @param entity
	 */
	public static <T> T merge(T entity)
	{
		return getEntityManager().merge(entity);
	}

	public static <T> void remove(T entity)
	{
		getEntityManager().remove(entity);

	}

	public static <T> void persist(T record)
	{
		getEntityManager().persist(record);

	}

	public static <T> void refresh(T record)
	{
		getEntityManager().refresh(record);

	}

	public static <T> void detach(T record)
	{
		getEntityManager().detach(record);

	}

	private static ThreadLocal<List<Runnable>> transientPostTransactionActions = new ThreadLocal<>();

	/**
	 * deprecated - use registerTransientPostAction(Runnable runnable)
	 *
	 * @param runnable
	 */
	@Deprecated
	public static void performAfterTransactionCompletes(Runnable runnable)
	{
		registerTransientPostAction(runnable);
	}

	/**
	 * Adds a runnable to the list of Actions that will be performed after the
	 * entity manager for this thread has been cleared.
	 *
	 * NOTE: as the EM has been cleared the Runnable must NOT try any database
	 * operations as they will fail.
	 *
	 * @param runnable
	 *            The Action to run when the em is cleared.
	 */
	public static void registerTransientPostAction(Runnable runnable)
	{
		List<Runnable> actionList = transientPostTransactionActions.get();
		if (actionList == null)
		{
			actionList = new LinkedList<>();
			transientPostTransactionActions.set(actionList);
		}
		actionList.add(runnable);

	}

	private static void runTransientPostActions()
	{
		try
		{
			List<Runnable> actions = transientPostTransactionActions.get();
			runRunnableActions(actions);
		}
		finally
		{
			// These actions are transient, so once run we clear them out.
			transientPostTransactionActions.set(null);
		}
	}

	private static void runRunnableActions(List<Runnable> actions)
	{
		if (actions != null)
			for (Runnable action : actions)
			{
				try
				{
					action.run();
				}
				catch (Throwable e)
				{
					logger.error(e, e);
				}
			}
	}

	/**
	 * Register an action that will be executed before begin is called on the
	 * transaction.
	 *
	 * Registered actions are global and will be run across all threads every
	 * time an entity manager is set via void
	 * setCurrentEntityManager(EntityManager em)
	 *
	 * @param action
	 *            The action to run before begin is called on a transaction.
	 */
	public static void registerPreAction(EMAction action)
	{
		synchronized (registeredPreActions)
		{
			registeredPreActions.add(action);
		}
	}

	/**
	 * Register an action that will be executed after commit is called on the
	 * transaction.
	 *
	 * Registered actions are global and will be run across all threads every
	 * time an entity manager is set via a call to
	 *
	 * @formatter:off void setCurrentEntityManager(EntityManager em)
	 * @formatter:on
	 *
	 * @param action
	 *            The action to run before begin is called on a transaction.
	 */
	public static void registerPostAction(Runnable action)
	{
		synchronized (registeredPostActions)
		{
			registeredPostActions.add(action);
		}
	}

	public static void runPreActions(EntityManager em)
	{
		synchronized (registeredPreActions)
		{
			runActions(registeredPreActions, em);
		}
	}

	public static void runPostActions()
	{
		runTransientPostActions();

		synchronized (registeredPostActions)
		{
			runRunnableActions(registeredPostActions);
		}

	}

	public static void runActions(List<EMAction> actions, EntityManager em)
	{
		if (actions != null)
			for (EMAction action : actions)
			{
				try
				{
					action.run(em);
				}
				catch (Throwable e)
				{
					logger.error(e, e);
				}
			}
	}

	static public abstract class EMAction
	{
		abstract public void run(EntityManager em);
	}

}
