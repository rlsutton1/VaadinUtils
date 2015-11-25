package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.internal.core.util.HandleFactory;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

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

	static Logger logger = LogManager.getLogger();

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
			EntityManager em = createEntityManager();

			try
			{

				setCurrentEntityManager(em);
				em.getTransaction().begin();

				T ret = worker.exec();

				em.getTransaction().commit();
				return ret;
			}
			finally
			{
				if (em.getTransaction().isActive())
				{
					em.getTransaction().rollback();
				}
				setCurrentEntityManager(null);
				em.close();
			}
		}
		// there was already an active entity manager, so just use it!
		return worker.exec();

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

		EntityManager entityManager = INSTANCE.emf.createEntityManager();

		return entityManager;
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
		try
		{
			getEntityManager().persist(record);
		}
		catch (Throwable e)
		{
			handleConstraintViolationException(e);
			throw e;
		}
	}

	public static <T> void refresh(T record)
	{
		getEntityManager().refresh(record);

	}

	public static <T> void detach(T record)
	{
		try
		{
			getEntityManager().detach(record);
		}
		catch (Throwable e)
		{
			handleConstraintViolationException(e);
			throw e;
		}

	}

	static void handleConstraintViolationException(Throwable e)
	{
		Throwable cause = e;
		if (e.getCause() != null)
		{
			cause = e.getCause();
		}
		if (cause instanceof ConstraintViolationException)
		{
			String groupedViolationMessage = e.getClass().getSimpleName() + " ";
			for (ConstraintViolation<?> violation : ((ConstraintViolationException) cause).getConstraintViolations())
			{
				logger.error(violation.getLeafBean().getClass().getCanonicalName() + " " + violation.getLeafBean());
				String violationMessage = violation.getLeafBean().getClass().getSimpleName() + " "
						+ violation.getPropertyPath() + " " + violation.getMessage() + ", the value was "
						+ violation.getInvalidValue();
				logger.error(violationMessage);
				groupedViolationMessage += violationMessage + "\n";
			}
		}
	}

}
