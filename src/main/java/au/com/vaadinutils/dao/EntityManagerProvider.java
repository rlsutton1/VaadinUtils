package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.errorHandling.ErrorWindow;

/**
 * The class is a place holder to allow access to an 'non-injected' entity
 * manager.
 * 
 * You need to initialise this Provider during application startup by calling
 * setEntityManagerFactory.
 * 
 * The for each request you need to inject a thread local EntityManager by
 * calling setCurrentEntityManager and then clearing it when the request
 * completes by calling setCurrentEntityManager with a null.
 * 
 * You should also wrap the entity in a Transaction block before calling
 * setCurrentEntityManager.
 * 
 * e.g.
 * 
 * EntityManager em = EntityManagerProvider.createEntityManager(); Transaction t
 * = null; Transaction t = new Transaction(em); try { // Create and set the
 * entity manager EntityManagerProvider.setCurrentEntityManager(em);
 * 
 * // Handle the request filterChain.doFilter(servletRequest, servletResponse);
 * 
 * t.commit(); } finally { if (t!= null) t.close(); // Reset the entity manager
 * EntityManagerProvider.setCurrentEntityManager(null); } }
 * 
 * 
 * You can use the @Link au.com.vaadinutils.filter.EntityManagerInjectorFilter
 * and @Link au.com.vaadinutils.filter.AtmosphereFilter to do the injection or
 * make up your own methods.
 * 
 * @author bsutton
 * 
 */
public enum EntityManagerProvider
{
	INSTANCE;

	private ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();
	private javax.persistence.EntityManagerFactory emf;

	static final Logger logger = LogManager.getLogger();

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
				setCurrentEntityManager(null);
				em.close();
			}
		}
		// there was already an active entity manager, so just use it!
		return worker.exec();

	}

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
		// return new EntityManagerWrapper(entityManager);
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

}
