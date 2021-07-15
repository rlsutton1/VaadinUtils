package au.com.vaadinutils.dao;

import java.util.Date;
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

import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.errorHandling.ErrorWindow;

public class EntityManagerTrackerWrapper implements EntityManager
{
	final private EntityManager em;

	Exception created = new Exception("Unclosed Entity Manager created here at " + new Date());

	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private ScheduledFuture<?> future;

	private static final AtomicLong idSeed = new AtomicLong();

	private final Long id;
	private Exception closedAt;

	EntityManagerTrackerWrapper(EntityManager em)
	{

		id = idSeed.incrementAndGet();
		logger.error("Created entityManager {}", id);
		this.em = em;
		Runnable runnable = new Runnable()
		{

			@Override
			public void run()
			{
				ErrorWindow.showErrorWindow(created);
			}
		};
		future = scheduler.scheduleWithFixedDelay(runnable, 40, 40, TimeUnit.SECONDS);

	}

	@Override
	public void persist(Object entity)
	{
		em.persist(entity);

	}

	@Override
	public <T> T merge(T entity)
	{

		return em.merge(entity);
	}

	@Override
	public void remove(Object entity)
	{

		em.remove(entity);

	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey)
	{
		if (closedAt != null)
		{
			logger.error("Trying to look up {} {}", entityClass, primaryKey);
			logger.error(closedAt, closedAt);
		}
		return em.find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)
	{

		return em.find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
	{

		return em.find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties)
	{

		return em.find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey)
	{

		return em.getReference(entityClass, primaryKey);
	}

	@Override
	public void flush()
	{

		em.flush();

	}

	@Override
	public void setFlushMode(FlushModeType flushMode)
	{

		em.setFlushMode(flushMode);

	}

	@Override
	public FlushModeType getFlushMode()
	{

		return em.getFlushMode();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode)
	{

		em.lock(entity, lockMode);

	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
	{

		em.lock(entity, lockMode, properties);

	}

	@Override
	public void refresh(Object entity)
	{

		em.refresh(entity);

	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties)
	{

		em.refresh(entity, properties);

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode)
	{

		em.refresh(entity, lockMode);

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties)
	{

		em.refresh(entity, lockMode, properties);

	}

	@Override
	public void clear()
	{

		em.clear();

	}

	@Override
	public void detach(Object entity)
	{

		em.detach(entity);

	}

	@Override
	public boolean contains(Object entity)
	{

		return em.contains(entity);
	}

	@Override
	public LockModeType getLockMode(Object entity)
	{

		return em.getLockMode(entity);
	}

	@Override
	public void setProperty(String propertyName, Object value)
	{

		em.setProperty(propertyName, value);

	}

	@Override
	public Map<String, Object> getProperties()
	{

		return em.getProperties();
	}

	@Override
	public Query createQuery(String qlString)
	{

		return em.createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
	{

		return em.createQuery(criteriaQuery);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery)
	{

		return em.createQuery(updateQuery);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery)
	{

		return em.createQuery(deleteQuery);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
	{

		return em.createQuery(qlString, resultClass);
	}

	@Override
	public Query createNamedQuery(String name)
	{

		return em.createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass)
	{

		return em.createNamedQuery(name, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString)
	{

		return em.createNativeQuery(sqlString);
	}

	@Override
	public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass)
	{

		return em.createNativeQuery(sqlString, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping)
	{

		return em.createNativeQuery(sqlString, resultSetMapping);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name)
	{

		return em.createNamedStoredProcedureQuery(name);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName)
	{

		return em.createStoredProcedureQuery(procedureName);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, @SuppressWarnings("rawtypes") Class... resultClasses)
	{

		return em.createStoredProcedureQuery(procedureName, resultClasses);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings)
	{

		return em.createStoredProcedureQuery(procedureName, resultSetMappings);
	}

	@Override
	public void joinTransaction()
	{

		em.joinTransaction();

	}

	@Override
	public boolean isJoinedToTransaction()
	{

		return em.isJoinedToTransaction();
	}

	@Override
	public <T> T unwrap(Class<T> cls)
	{

		return em.unwrap(cls);
	}

	@Override
	public Object getDelegate()
	{

		return em.getDelegate();
	}

	@Override
	public void close()
	{
		logger.error("Closed entityManager {}", id);

		future.cancel(false);
		closedAt = new Exception("Closed here at " + new Date());
		if (em.getTransaction().isActive())
		{
			logger.error("Transaction is still active at close");
			em.getTransaction().commit();
		}
		try
		{
			em.close();
		}
		catch (Throwable e)
		{
			logger.error(e, e);
		}

	}

	@Override
	public boolean isOpen()
	{

		return em.isOpen();
	}

	@Override
	public EntityTransaction getTransaction()
	{
		logger.error("Get Transaction Called");
		return em.getTransaction();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory()
	{

		return em.getEntityManagerFactory();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder()
	{
		if (closedAt != null)
		{
			logger.error("Trying to getCriteriaBuilder");
			logger.error(closedAt, closedAt);
		}
		return em.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel()
	{

		return em.getMetamodel();
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType)
	{

		return em.createEntityGraph(rootType);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName)
	{

		return em.createEntityGraph(graphName);
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName)
	{

		return em.getEntityGraph(graphName);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass)
	{

		return em.getEntityGraphs(entityClass);
	}

}
