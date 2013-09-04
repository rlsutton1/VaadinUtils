package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;


public enum EntityManagerProvider 
{
	INSTANCE;
	
	
	public EntityManager getEntityManager()
	{
		return entityManagerThreadLocal.get();
	}

	public void setCurrentEntityManager(EntityManager em)
	{
		entityManagerThreadLocal.set(em);
	}

	private  ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();
}