package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;


/**
 * The class is a place holder to allow access to an 'non-injected' entity manager.
 * 
 * You need to initialise this Provider during application startup 
 * by calling setEntityManagerFactory.
 * 
 * The for each request you need to inject a thread local EntityManager by calling
 * setCurrentEntityManager and then clearing it when the request completes by 
 * calling setCurrentEntityManager with a null.
 * 
 * You should also wrap the entity in a Transaction block before calling
 * setCurrentEntityManager.
 * 
 * e.g.
 * 
 * 		Transaction t = new Transaction(em);
		try 
		{
			// Create and set the entity manager
			EntityManagerProvider.INSTANCE.setCurrentEntityManager(em);

			// Handle the request
			filterChain.doFilter(servletRequest, servletResponse);

			t.commit();
		}
		finally
		{
			if (t!= null)
				t.close();
			// Reset the entity manager
			EntityManagerProvider.INSTANCE.setCurrentEntityManager(null);
		}
	}

 * 
 * You can use the @Link au.com.vaadinutils.filter.EntityManagerInjectorFilter
 * and @Link au.com.vaadinutils.filter.AtmosphereFilter to do the injection
 * or make up your own methods.
 *  
 * @author bsutton
 *
 */
public enum EntityManagerProvider 
{
	INSTANCE;
	
	private  ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();
	private javax.persistence.EntityManagerFactory emf;

	
	public EntityManager getEntityManager()
	{
		return entityManagerThreadLocal.get();
	}

	public void setCurrentEntityManager(EntityManager em)
	{
		entityManagerThreadLocal.set(em);
	}

	
	/**
	 * Call this method to initialise the EntityManagerProvider so that it can hand
	 * out EntityManagers to worker threads.
	 * 
	 * This should normally be called from a servlet Context Listener.
	 * 
	 * @param emf
	 */
	public void setEntityManagerFactory(javax.persistence.EntityManagerFactory emf)
	{
		this.emf = emf;
	}

	/**
	 * If you have a worker thread then it won't have access to a thread local
	 * entity manager (as they are injected by the servlet request filters
	 * mentioned above. For worker threads you need to call this method
	 * to get an entity manager.
	 * You will also need 
	 * @return
	 */
	public EntityManager createEntityManager()
	{
		if (this.emf == null)
		{
			throw new IllegalStateException("Context is not initialized yet.");
		}

		return this.emf.createEntityManager();
	}


}