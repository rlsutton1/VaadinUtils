package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;


/**
 * The class is a place holder to allow access to an 'non-injected' entity manager.
 * 
 * You should implement a servlet filter which calls setCurrentEntityManager 
 * @author bsutton
 *
 */
public enum EntityManagerProvider implements EntityManagerFactory
{
	INSTANCE;
	
	private  ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

	
	
	public EntityManager getEntityManager()
	{
		return entityManagerThreadLocal.get();
	}

	public void setCurrentEntityManager(EntityManager em)
	{
		entityManagerThreadLocal.set(em);
	}

}