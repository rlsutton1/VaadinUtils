package au.com.vaadinutils.dao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityManagerWrapper implements EntityManager
{
	final private EntityManager em;

	final static AtomicLong seen = new AtomicLong();

	Logger logger = LogManager.getLogger();

	private long emid;

	final private ScheduledFuture<?> future;
	
	private final static ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
	

	EntityManagerWrapper(EntityManager em)
	{
		emid = seen.incrementAndGet();
		this.em = em;
//		logger.error("Created entity Manager " + emid);
		
		final Exception here = new Exception("Entity Manager Still Open");
		
		future = ex.scheduleAtFixedRate(new Runnable()
		{
			
			@Override
			public void run()
			{
				logger.error(here,here);
				
			}
		},1, 1,TimeUnit.MINUTES);
		
	}

	@Override
	public void persist(Object entity)
	{
		em.persist(entity);
//		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

	}

	@Override
	public <T> T merge(T entity)
	{
//		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.merge(entity);
	}

	@Override
	public void remove(Object entity)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.remove(entity);

	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey)
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getReference(entityClass, primaryKey);
	}

	@Override
	public void flush()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.flush();

	}

	@Override
	public void setFlushMode(FlushModeType flushMode)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.setFlushMode(flushMode);

	}

	@Override
	public FlushModeType getFlushMode()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getFlushMode();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.lock(entity, lockMode);

	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.lock(entity, lockMode, properties);

	}

	@Override
	public void refresh(Object entity)
	{
//		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.refresh(entity);

	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.refresh(entity, properties);

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.refresh(entity, lockMode);

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.refresh(entity, lockMode, properties);

	}

	@Override
	public void clear()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.clear();

	}

	@Override
	public void detach(Object entity)
	{
//		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.detach(entity);

	}

	@Override
	public boolean contains(Object entity)
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.contains(entity);
	}

	@Override
	public LockModeType getLockMode(Object entity)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getLockMode(entity);
	}

	@Override
	public void setProperty(String propertyName, Object value)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.setProperty(propertyName, value);

	}

	@Override
	public Map<String, Object> getProperties()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getProperties();
	}

	@Override
	public Query createQuery(String qlString)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createQuery(criteriaQuery);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createQuery(updateQuery);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createQuery(deleteQuery);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createQuery(qlString, resultClass);
	}

	@Override
	public Query createNamedQuery(String name)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createNamedQuery(name, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createNativeQuery(sqlString);
	}

	@Override
	public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createNativeQuery(sqlString, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createNativeQuery(sqlString, resultSetMapping);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createNamedStoredProcedureQuery(name);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createStoredProcedureQuery(procedureName);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, @SuppressWarnings("rawtypes") Class... resultClasses)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createStoredProcedureQuery(procedureName, resultClasses);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createStoredProcedureQuery(procedureName, resultSetMappings);
	}

	@Override
	public void joinTransaction()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		em.joinTransaction();

	}

	@Override
	public boolean isJoinedToTransaction()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.isJoinedToTransaction();
	}

	@Override
	public <T> T unwrap(Class<T> cls)
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.unwrap(cls);
	}

	@Override
	public Object getDelegate()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getDelegate();
	}

	@Override
	public void close()
	{
//		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		if (em.getTransaction().isActive())
		{
			Exception e = new Exception("Closing entity manager with open transaction");
			logger.error(e,e);
		}
		
		em.close();
		future.cancel(false);

	}

	@Override
	public boolean isOpen()
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.isOpen();
	}

	@Override
	public EntityTransaction getTransaction()
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getTransaction();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory()
	{
//		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getEntityManagerFactory();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder()
	{
	//	logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel()
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getMetamodel();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createEntityGraph(rootType);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.createEntityGraph(graphName);
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getEntityGraph(graphName);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass)
	{
		logger.error("Thread: {} using entity Manager {}", Thread.currentThread().getId(), emid);

		return em.getEntityGraphs(entityClass);
	}

}
