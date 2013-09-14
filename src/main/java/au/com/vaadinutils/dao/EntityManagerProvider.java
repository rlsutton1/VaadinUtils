package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;

import au.com.vaadinutils.crud.EntityManagerFactory;


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