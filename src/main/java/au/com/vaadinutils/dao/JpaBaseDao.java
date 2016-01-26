package au.com.vaadinutils.dao;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;

public class JpaBaseDao<E, K> implements Dao<E, K>
{
	protected Class<E> entityClass;

	public interface Condition<E>
	{

		Condition<E> and(Condition<E> c1);

		Predicate getPredicates();

		Condition<E> or(Condition<E> c1);

	}

	static public <E> JpaBaseDao<E, Long> getGenericDao(Class<E> class1)
	{
		return new JpaBaseDao<E, Long>(class1);

	}

	@SuppressWarnings("unchecked")
	public JpaBaseDao()
	{

		// hack to get the derived classes Class type.
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		Preconditions.checkNotNull(genericSuperclass);
		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
		Preconditions.checkNotNull(this.entityClass);
	}

	/**
	 * it's very important that we don't retain a reference to the
	 * entitymanager, as when you instance this class and then use it in a
	 * closure you will end up trying to access a closed entitymanager
	 *
	 * @return
	 */
	protected static EntityManager getEntityManager()
	{
		EntityManager em = EntityManagerProvider.getEntityManager();

		Preconditions.checkNotNull(em,
				"Entity manager has not been initialized, if you are using a worker thread you will have to call EntityManagerProvider.createEntityManager()");

		Preconditions.checkState(em.isOpen(),
				"The entity manager is closed, this can happen if you instance this class "
						+ "and then use it in a closure when the closure gets called on a "
						+ "separate thread or servlet request");

		return em;

	}

	public JpaBaseDao(Class<E> class1)
	{
		entityClass = class1;
	}

	@Override
	public void persist(E entity)
	{
		getEntityManager().persist(entity);
	}

	@Override
	public E merge(E entity)
	{
		return getEntityManager().merge(entity);
	}

	@Override
	public void remove(E entity)
	{
		getEntityManager().remove(entity);

	}

	@SuppressWarnings("unchecked")
	public E findById(Integer id)
	{
		return findById((K) new Long(id));
	}

	@Override
	public E findById(K id)
	{
		return getEntityManager().find(entityClass, id);
	}

	protected E findSingleBySingleParameter(String queryName, SingularAttribute<E, String> paramName, String paramValue)
	{
		E entity = null;
		Query query = getEntityManager().createNamedQuery(queryName);
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
		Query query = getEntityManager().createNamedQuery(queryName);
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
		Query query = getEntityManager().createNamedQuery(queryName);
		query.setParameter(paramName, paramValue);
		@SuppressWarnings("unchecked")
		List<E> entities = query.getResultList();
		return entities;
	}

	/**
	 * Runs the given query returning all entities that matched by the query.
	 *
	 * @param queryName
	 * @return
	 */
	protected List<E> findList(String queryName)
	{
		Query query = getEntityManager().createNamedQuery(queryName);
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
	 * You may pass in an array of attributes and a order by clause will be
	 * added for each attribute in turn e.g. order by order[0], order[1] ....
	 */
	@Override
	public List<E> findAll(SingularAttribute<E, ?> order[])
	{
		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		if (order != null)
		{
			List<Order> ordering = new LinkedList<Order>();
			for (SingularAttribute<E, ?> field : order)
			{
				ordering.add(builder.asc(root.get(field)));

			}
			criteria.orderBy(ordering);
		}
		List<E> results = getEntityManager().createQuery(criteria).getResultList();

		return results;

	}

	/**
	 * Returns all rows ordered by the given set of entity attribues.
	 *
	 * @param order
	 *            You may pass in an array of attributes and a order by clause
	 *            will be added for each attribute in turn e.g. order by
	 *            order[0], order[1] ....
	 *
	 * @param sortAscending
	 *            An array of booleans that must be the same size as the order.
	 *            The sort array controls whether each attribute will be sorted
	 *            ascending or descending.
	 *
	 */
	public List<E> findAll(SingularAttribute<E, ?> order[], boolean sortAscending[])
	{
		Preconditions.checkArgument(order.length == sortAscending.length,
				"Both arguments must have the same no. of array elements.");
		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);

		List<Order> ordering = new LinkedList<Order>();
		for (SingularAttribute<E, ?> field : order)
		{
			if (sortAscending[ordering.size()] == true)
				ordering.add(builder.asc(root.get(field)));
			else
				ordering.add(builder.desc(root.get(field)));

		}
		criteria.orderBy(ordering);

		List<E> results = getEntityManager().createQuery(criteria).getResultList();

		return results;

	}

	public <V> E findOneByAttribute(SingularAttribute<E, V> vKey, V value)
	{
		E ret = null;
		List<E> results = findAllByAttribute(vKey, value, null, 1);
		if (results.size() > 0)
		{
			ret = results.get(0);
		}

		return ret;
	}

	public <V, SK> List<E> findAllByAttribute(SingularAttribute<E, V> vKey, V value, SingularAttribute<E, SK> order)
	{
		return findAllByAttribute(vKey, value, order, null);
	}

	public <V, SK> List<E> findAllByAttribute(SingularAttribute<E, V> vKey, V value, SingularAttribute<E, SK> order,
			Integer limit)
	{

		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		criteria.where(builder.equal(root.get(vKey), value));
		if (order != null)
		{
			criteria.orderBy(builder.asc(root.get(order)));
		}

		TypedQuery<E> query = getEntityManager().createQuery(criteria);
		if (limit != null)
		{
			query = query.setMaxResults(limit);
		}
		return query.getResultList();

	}

	public <SK> List<E> findAllByAttributeLike(SingularAttribute<E, String> vKey, String value,
			SingularAttribute<E, SK> order)
	{

		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);
		criteria.select(root);
		criteria.where(builder.like(root.<String> get(vKey), value));
		if (order != null)
		{
			criteria.orderBy(builder.asc(root.get(order)));
		}
		List<E> results = getEntityManager().createQuery(criteria).getResultList();

		return results;

	}

	/**
	 * Find a single record by multiple attributes. Searches using AND.
	 *
	 * @param attributes
	 *            AttributeHashMap of SingularAttributes and values
	 * @return first matching entity
	 */
	public E findOneByAttributes(AttributesHashMap<E> attributes)
	{
		E ret = null;
		List<E> results = findAllByAttributes(attributes, null);
		if (results.size() > 0)
		{
			ret = results.get(0);
		}

		return ret;
	}

	/**
	 * Find multiple records by multiple attributes. Searches using AND.
	 *
	 * @param <SK>
	 *            attribute
	 * @param attributes
	 *            AttributeHashMap of SingularAttributes and values
	 * @param order
	 *            SingularAttribute to order by
	 * @return a list of matching entities
	 */
	public <SK> List<E> findAllByAttributes(AttributesHashMap<E> attributes, SingularAttribute<E, SK> order)
	{

		final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		final CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		final Root<E> root = criteria.from(entityClass);
		criteria.select(root);

		Predicate where = builder.conjunction();
		for (Entry<SingularAttribute<E, Object>, Object> attr : attributes.entrySet())
		{
			where = builder.and(where, builder.equal(root.get(attr.getKey()), attr.getValue()));
		}
		criteria.where(where);

		if (order != null)
		{
			criteria.orderBy(builder.asc(root.get(order)));
		}
		List<E> results = getEntityManager().createQuery(criteria).getResultList();

		return results;
	}

	/**
	 * Find a single record by multiple attributes. Searches using OR.
	 *
	 * @param attributes
	 *            AttributeHashMap of SingularAttributes and values
	 * @return first matching entity
	 */
	public E findOneByAnyAttributes(AttributesHashMap<E> attributes)
	{
		E ret = null;
		List<E> results = findAllByAnyAttributes(attributes, null);
		if (results.size() > 0)
		{
			ret = results.get(0);
		}

		return ret;
	}

	/**
	 * Find multiple records by multiple attributes. Searches using OR.
	 *
	 * @param <SK>
	 *            attribute
	 * @param attributes
	 *            AttributeHashMap of SingularAttributes and values
	 * @param order
	 *            SingularAttribute to order by
	 * @return a list of matching entities
	 */
	public <SK> List<E> findAllByAnyAttributes(AttributesHashMap<E> attributes, SingularAttribute<E, SK> order)
	{

		final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		final CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		final Root<E> root = criteria.from(entityClass);
		criteria.select(root);

		Predicate where = builder.conjunction();
		for (Entry<SingularAttribute<E, Object>, Object> attr : attributes.entrySet())
		{
			where = builder.or(where, builder.equal(root.get(attr.getKey()), attr.getValue()));
		}
		criteria.where(where);

		if (order != null)
		{
			criteria.orderBy(builder.asc(root.get(order)));
		}
		List<E> results = getEntityManager().createQuery(criteria).getResultList();

		return results;
	}

	/**
	 * get count of entity with a simple criteria
	 *
	 * @param vKey
	 * @param value
	 * @return
	 */
	public <V> Long getCount(SingularAttribute<E, V> vKey, V value)
	{
		CriteriaBuilder qb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		Root<E> root = cq.from(entityClass);
		cq.select(qb.count(root));
		cq.where(qb.equal(root.get(vKey), value));

		return getEntityManager().createQuery(cq).getSingleResult();
	}

	public JPAContainer<E> createVaadinContainer()
	{
		JPAContainer<E> container = new JPAContainer<E>(entityClass);
		container.setEntityProvider(new BatchingPerRequestEntityProvider<E>(entityClass));
		return container;

	}

	public EntityContainer<E> createLazyQueryContainer()
	{
		EntityManager em = getEntityManager();
		boolean compositeItmes = true;

		boolean detachedEntities = true;
		String propertyId = getIdField().getName();
		boolean applicationManagedTransactions = true;
		EntityContainer<E> entityContainer = new EntityContainer<E>(em, entityClass, propertyId, Integer.MAX_VALUE,
				applicationManagedTransactions, detachedEntities, compositeItmes);

		for (Attribute<? super E, ?> attrib : getIdField().getDeclaringType().getAttributes())
		{
			entityContainer.addContainerProperty(attrib.getName(), attrib.getJavaType(), null, true, true);
		}

		return entityContainer;

	}

	static public <T> SingularAttribute<T, Long> getIdField(Class<T> type)
	{
		Metamodel metaModel = getEntityManager().getMetamodel();
		EntityType<T> entityType = metaModel.entity(type);
		return entityType.getDeclaredId(Long.class);
	}

	public SingularAttribute<E, Long> getIdField()
	{
		return getIdField(entityClass);

	}

	public JPAContainer<E> createVaadinContainer(final int sizeLimit)
	{
		JPAContainer<E> container = new JPAContainer<E>(entityClass)
		{
			private static final long serialVersionUID = -3280358604354247501L;

			@Override
			public int size()
			{
				int size = super.size();
				return Math.min(sizeLimit, size);
			}
		};
		container.setEntityProvider(new BatchingPerRequestEntityProvider<E>(entityClass));
		return container;

	}

	public void flushCache()
	{
		getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);

	}

	public JPAContainer<E> createVaadinContainerAndFlushCache(final int sizeLimit)
	{
		getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
		return createVaadinContainer(sizeLimit);
	}

	public JPAContainer<E> createVaadinContainerAndFlushCache()
	{
		getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
		return createVaadinContainer();
	}

	public int deleteAll()
	{
		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaDelete<E> criteria = builder.createCriteriaDelete(entityClass);
		int result = getEntityManager().createQuery(criteria).executeUpdate();

		flushCache();

		return result;

	}

	public <V> int deleteAllByAttribute(SingularAttribute<E, V> vKey, V value)
	{

		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaDelete<E> criteria = builder.createCriteriaDelete(entityClass);

		Root<E> root = criteria.from(entityClass);

		criteria.where(builder.equal(root.get(vKey), value));

		int result = getEntityManager().createQuery(criteria).executeUpdate();

		return result;

	}

	public <V, J> List<E> findAllByAttributeJoin(SingularAttribute<E, J> joinAttr, SingularAttribute<J, V> vKey,
			V value, JoinType joinType)
	{

		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);

		Root<E> root = criteria.from(entityClass);

		Join<E, J> join = root.join(joinAttr, joinType);

		criteria.where(builder.equal(join.get(vKey), value));

		return getEntityManager().createQuery(criteria).getResultList();

	}

	public <V, J> int deleteAllByAttributeJoin(SingularAttribute<J, V> vKey, V value, SingularAttribute<E, J> joinAttr)
	{

		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

		CriteriaDelete<E> criteria = builder.createCriteriaDelete(entityClass);

		Root<E> root = criteria.from(entityClass);

		Join<E, J> join = root.join(joinAttr, JoinType.LEFT);

		criteria.where(builder.equal(join.get(vKey), value));

		getEntityManager().getClass();
		int result = getEntityManager().createQuery(criteria).executeUpdate();

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
		Query query = getEntityManager().createQuery(qry);
		Number countResult = (Number) query.getSingleResult();
		return countResult.longValue();

	}

	public void flush()
	{
		getEntityManager().flush();

	}

	public void refresh(E entity)
	{
		getEntityManager().refresh(entity);
	}

	public void detach(E entity)
	{
		getEntityManager().detach(entity);

	}

	public JpaBaseDao<E, K>.FindBuilder findOld()
	{
		return new FindBuilder();
	}

	public JpaDslBuilder<E> find()
	{
		return new JpaDslBuilder<E>(entityClass);
	}
	
	public JpaDslTupleBuilder<E> findTuple()
	{
		return new JpaDslTupleBuilder<E>(entityClass);
	}

	public JpaDslBuilder<E> jpaContainerDelegate(CriteriaQuery<E> criteria)
	{
		return new JpaDslBuilder<E>(criteria, entityClass);
	}

	public class FindBuilder
	{
		CriteriaBuilder builder = EntityManagerProvider.getEntityManager().getCriteriaBuilder();

		CriteriaQuery<E> criteria = builder.createQuery(entityClass);
		Root<E> root = criteria.from(entityClass);
		List<Predicate> predicates = new LinkedList<>();

		private Integer limit = null;

		private Integer startPosition = null;

		/**
		 * specify that JPA should fetch child entities in a single query!
		 *
		 * @param field
		 * @return
		 */
		public <L> FindBuilder fetch(SingularAttribute<E, L> field)
		{
			root.fetch(field, JoinType.LEFT);
			return this;
		}

		public <L> FindBuilder whereEqual(SingularAttribute<E, L> field, L value)
		{
			predicates.add(builder.equal(root.get(field), value));
			return this;
		}

		public <J, L> FindBuilder joinWhereEqual(Join<E, J> join, SingularAttribute<J, L> field, L value)
		{
			predicates.add(builder.equal(join.get(field), value));
			return this;
		}

		public FindBuilder whereLike(SingularAttribute<E, String> field, String value)
		{
			predicates.add(builder.like(root.get(field), value));
			return this;
		}

		public <L extends Comparable<? super L>> FindBuilder whereGreaterThan(SingularAttribute<E, L> field, L value)
		{
			predicates.add(builder.greaterThan(root.get(field), value));
			return this;
		}

		public <L extends Comparable<? super L>> FindBuilder whereGreaterThanOrEqualTo(SingularAttribute<E, L> field,
				L value)
		{

			predicates.add(builder.greaterThanOrEqualTo(root.get(field), value));
			return this;
		}

		public FindBuilder limit(int limit)
		{
			this.limit = limit;
			return this;
		}

		public FindBuilder startPosition(int startPosition)
		{
			this.startPosition = startPosition;
			return this;
		}

		public FindBuilder orderBy(SingularAttribute<E, ?> field, boolean asc)
		{
			if (asc)
			{
				criteria.orderBy(builder.asc(root.get(field)));
			}
			else
			{
				criteria.orderBy(builder.desc(root.get(field)));
			}
			return this;
		}

		public <J> FindBuilder joinOrderBy(Join<E, J> join, SingularAttribute<J, ?> field, boolean asc)
		{
			if (asc)
			{
				criteria.orderBy(builder.asc(join.get(field)));
			}
			else
			{
				criteria.orderBy(builder.desc(join.get(field)));
			}
			return this;
		}

		FindBuilder()
		{
			criteria.select(root);
		}

		public E getSingleResult()
		{
			limit(1);
			TypedQuery<E> query = prepareQuery();

			return query.getSingleResult();
		}

		public List<E> getResultList()
		{
			TypedQuery<E> query = prepareQuery();
			return query.getResultList();
		}

		private TypedQuery<E> prepareQuery()
		{
			Predicate filter = null;
			for (Predicate predicate : predicates)
			{
				if (filter == null)
				{
					filter = predicate;
				}
				else
				{
					filter = builder.and(filter, predicate);
				}

			}
			if (filter != null)
			{
				criteria.where(filter);
			}
			TypedQuery<E> query = EntityManagerProvider.getEntityManager().createQuery(criteria);
			if (limit != null)
			{
				query.setMaxResults(limit);
			}
			if (startPosition != null)
			{
				query.setFirstResult(startPosition);
			}
			return query;
		}

		public <L> FindBuilder whereNotEqueal(SingularAttribute<E, L> field, L value)
		{
			predicates.add(builder.notEqual(root.get(field), value));
			return this;

		}

		public <L> FindBuilder whereNotNull(SingularAttribute<E, L> field)
		{
			predicates.add(builder.isNotNull(root.get(field)));
			return this;

		}

		public <L> FindBuilder whereNull(SingularAttribute<E, L> field)
		{
			predicates.add(builder.isNull(root.get(field)));
			return this;

		}

		public Predicate like(SingularAttribute<E, String> field, String value)
		{
			return builder.like(root.get(field), value);

		}

		public <J> Join<E, J> join(SingularAttribute<E, J> joinAttribute, JoinType joinType)
		{

			return root.join(joinAttribute, joinType);

		}

		public <J> Predicate joinLike(Join<E, J> join, SingularAttribute<J, String> field, String value)
		{
			return builder.like(join.get(field), value);

		}

		public FindBuilder whereAnd(Predicate pred)
		{
			predicates.add(pred);
			return this;
		}

		public FindBuilder whereOr(List<Predicate> orPredicates)
		{
			Predicate or = null;
			for (Predicate pred : orPredicates)
			{
				if (or == null)
				{
					or = pred;
				}
				else
				{
					or = builder.or(or, pred);
				}
			}
			if (or != null)
			{
				predicates.add(or);
			}
			return this;

		}

		public <L extends Comparable<? super L>> FindBuilder whereLessThanOrEqualTo(SingularAttribute<E, L> field,
				L value)
		{
			predicates.add(builder.lessThanOrEqualTo(root.get(field), value));

			return this;

		}

		public <L extends Comparable<? super L>> Predicate greaterThanOrEqualTo(SingularAttribute<E, L> field, L value)
		{
			return builder.greaterThanOrEqualTo(root.get(field), value);

		}

		public <L> Predicate isNull(SingularAttribute<E, L> field)
		{
			return builder.isNull(root.get(field));

		}

	}

}
