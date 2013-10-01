package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;

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

	public static EntityManager getEntityManager()
	{
		return INSTANCE.entityManagerThreadLocal.get();
	}

	public static void setCurrentEntityManager(EntityManager em)
	{
		INSTANCE.entityManagerThreadLocal.set(em);
	}

	/**
	 * Call this method to initialise the EntityManagerProvider so that it can
	 * hand out EntityManagers to worker threads.
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
	 * If you have a worker thread then it won't have access to a thread local
	 * entity manager (as they are injected by the servlet request filters
	 * mentioned above. For worker threads you need to call this method to get
	 * an entity manager. You will also need
	 * 
	 * @return
	 */
	public static EntityManager createEntityManager()
	{
		if (INSTANCE.emf == null)
		{
			throw new IllegalStateException("Context is not initialized yet.");
		}

		return INSTANCE.emf.createEntityManager();
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

}