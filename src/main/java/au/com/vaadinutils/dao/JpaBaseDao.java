package au.com.vaadinutils.dao;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;

public class JpaBaseDao<E, K> implements Dao<E, K>
{
	protected Class<E> entityClass;

	protected EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public JpaBaseDao()
	{
		this.entityManager = EntityManagerProvider.getEntityManager();
		Preconditions
				.checkNotNull(
						this.entityManager,
						"Entity manager has not been initialized, if you are using a worker thread you will have to call EntityManagerProvider.createEntityManager()");

		// hack to get the derived classes Class type.
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		Preconditions.checkNotNull(genericSuperclass);
		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
		Preconditions.checkNotNull(this.entityClass);
	}

	@SuppressWarnings("unchecked")
	public JpaBaseDao(EntityManager em)
	{
		this.entityManager = em;
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
	}

	public JpaBaseDao(Class<E> class1)
	{
		this.entityManager = EntityManagerProvider.getEntityManager();
		Preconditions
				.checkNotNull(
						this.entityManager,
						"Entity manager has not been initialized, if you are using a worker thread you will have to call EntityManagerProvider.createEntityManager()");
		entityClass = class1;
	}

	public void persist(E entity)
	{
		entityManager.persist(entity);
	}

	public E merge(E entity)
	{
		return entityManager.merge(entity);
	}

	public void remove(E entity)
	{
		entityManager.remove(entity);
	}

	public E findById(K id)
	{
		return entityManager.find(entityClass, id);
	}

	protected E findSingleBySingleParameter(String queryName, SingularAttribute<E, String> paramName, String paramValue)
	{
		E entity = null;
		Query query = entityManager.createNamedQuery(queryName);
		query.setParameter(paramName.getName(), paramValue);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<E> entities = query.getResultList();
		if (entities.size() > 0)
			entity = entities.get(0);
		return entity;
	}

	protected E findSingleBySingleParameter(String queryName, String paramName, String paramValue)
	{
		E entity = null;
		Query query = entityManager.createNamedQuery(queryName);
		query.setParameter(paramName, paramValue);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<E> entities = query.getResultList();
		if (entities.size() > 0)
			entity = entities.get(0);
		return entity;
	}

	protected List<E> findListBySingleParameter(String queryName, String paramName, Object paramValue)
	{
		Query query = entityManager.createNamedQuery(queryName);
		query.setParameter(paramName, paramValue);
		@SuppressWarnings("unchecked")
		List<E> entities = query.getResultList();
		return entities;
	}

	/**
	 * Runs the given query returning all entities that matched by the query.
	 * @param queryName
	 * @return
	 */
	protected List<E> findList(String queryName)
	{
		Query query = entityManager.createNamedQuery(queryName);
		@SuppressWarnings("unchecked")
		List<E> entities = query.getResultList();
		return entities;
	}


	@Override
	public List<E> findAll()
	{
		return findAll(null);
	}

	/**
	 * Returns all rows ordered by the given set of entity attribues.
	 * 
	 * You may pass in an array of attributes and a order by clause 
	 * will be added for each attribute in turn
	 * e.g.
	 *  order by order[0], order[1] ....
	 */
	@Override
	public List<E> findAll(SingularAttribute<E, ?> order[])
	{
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		if (order != null)
		{
			List<Order> ordering = new LinkedList<Order>(); 
			for (SingularAttribute<E, ?> field : order)
			{
				ordering.add( builder.asc(root.get(field)));
				
			}
			criteria.orderBy(ordering);
		}
		List<E> results = entityManager.createQuery(criteria).getResultList();

		return results;

	}

	/**
	 * Returns all rows ordered by the given set of entity attribues.
	 * 
	 * @param order
	 * You may pass in an array of attributes and a order by clause 
	 * will be added for each attribute in turn
	 * e.g.
	 *  order by order[0], order[1] ....
	 *  
	 * @param sortAscending
	 * An array of booleans that must be the same size as the order. The sort
	 * array controls whether each attribute will be sorted ascending or descending.
	 *  
	 */
	public List<E> findAll(SingularAttribute<E, ?> order[], boolean sortAscending[])
	{
		Preconditions.checkArgument(order.length == sortAscending.length, "Both arguments must have the same no. of array elements.");
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		if (order != null)
		{
			List<Order> ordering = new LinkedList<Order>(); 
			for (SingularAttribute<E, ?> field : order)
			{
				if (sortAscending[ordering.size()] == true)
					ordering.add( builder.asc(root.get(field)));
				else
					ordering.add( builder.desc(root.get(field)));
				
			}
			criteria.orderBy(ordering);
		}
		List<E> results = entityManager.createQuery(criteria).getResultList();

		return results;

	}

	/**
	 * Returns all rows ordered by the given set of entity attribues.
	 * 
	 * @param order
	 * You may pass in an array of attributes and a order by clause 
	 * will be added for each attribute in turn
	 * e.g.
	 *  order by order[0], order[1] ....
	 *  
	 * @param sortAscending
	 * An array of booleans that must be the same size as the order. The sort
	 * array controls whether each attribute will be sorted ascending or descending.
	 *  
	 */
	public List<E> findAll(SingularAttribute<E, ?> order[], boolean sortAscending[])
	{
		Preconditions.checkArgument(order.length == sortAscending.length, "Both arguments must have the same no. of array elements.");
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		if (order != null)
		{
			List<Order> ordering = new LinkedList<Order>(); 
			for (SingularAttribute<E, ?> field : order)
			{
				if (sortAscending[ordering.size()] == true)
					ordering.add( builder.asc(root.get(field)));
				else
					ordering.add( builder.desc(root.get(field)));
				
			}
			criteria.orderBy(ordering);
		}
		List<E> results = entityManager.createQuery(criteria).getResultList();

		return results;

	}

	public <V> E findOneByAttribute(SingularAttribute<E, V> vKey, V value)
	{
		E ret = null;
		List<E> results = findAllByAttribute(vKey, value, null);
		if (results.size() > 0)
		{
			ret = results.get(0);
		}

		return ret;
	}

	public <V, SK> List<E> findAllByAttribute(SingularAttribute<E, V> vKey, V value, SingularAttribute<E, SK> order)
	{

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		criteria.where(builder.equal(root.get(vKey), value));
		if (order != null)
		{
			criteria.orderBy(builder.asc(root.get(order)));
		}
		List<E> results = entityManager.createQuery(criteria).getResultList();

		return results;

	}

	public JPAContainer<E> createVaadinContainer()
	{
		JPAContainer<E> container = new JPAContainer<E>(entityClass);
		container.setEntityProvider(new BatchingPerRequestEntityProvider<E>(entityClass));
		return container;

	}
	
	public JPAContainer<E> createVaadinContainerAndFlushCache()
	{
		entityManager.getEntityManagerFactory().getCache().evict(entityClass);
		return createVaadinContainer();
	}

	@SuppressWarnings("unused")
	private void oldCreateVaadinContainer()
	{
		JPAContainerFactory.makeBatchable(entityClass, entityManager);
	}

	public <V> int deleteAllByAttribute(SingularAttribute<E, V> vKey, V value)
	{

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaDelete<E> criteria = builder.createCriteriaDelete(entityClass);

		Root<E> root = criteria.from(entityClass);

		criteria.where(builder.equal(root.get(vKey), value));

		entityManager.getClass();
		int result = entityManager.createQuery(criteria).executeUpdate();

		return result;

	}

	/**
	 * @return the number of entities in the table.
	 */
	public long getCount()
	{
		String entityName = entityClass.getSimpleName();
		Table annotation = entityClass.getAnnotation(Table.class);
		String tableName;
		if (annotation != null)
			tableName = annotation.name();
		else
			tableName = entityName;

		String qry = "select count(" + entityName + ") from " + tableName + " " + entityName;
		Query query = entityManager.createQuery(qry);
		Number countResult = (Number) query.getSingleResult();
		return countResult.longValue();

	}

	public void flush()
	{
		this.entityManager.flush();

	}

	public void refresh(E entity)
	{
		this.entityManager.refresh(entity);	
	}

	public void detach(E entity)
	{
		this.entityManager.detach(entity);
		
	}

}
