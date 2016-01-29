package au.com.vaadinutils.dao;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.google.common.base.Preconditions;

import au.com.vaadinutils.dao.JpaBaseDao.Condition;

/**
 * 
 * @author rsutton
 *
 * @param <E>
 *            - Entity Type the Query is based on
 * @param <R>
 *            - Return Type - usually the same as the Entity Type, but in the
 *            case of a Tuple query then it would be Tuple
 */
public abstract class JpaDslAbstract<E, R>
{

	protected CriteriaBuilder builder;

	private Integer limit = null;

	Predicate predicate = null;

	private Integer startPosition = null;
	protected CriteriaQuery<R> criteria;
	protected Class<E> entityClass;

	/**
	 * used to check that the entityManager doesn't shift under our feet!!!
	 */
	final private EntityManager dontUseThis = EntityManagerProvider.getEntityManager();

	/**
	 * it's very important that we don't retain a reference to the
	 * entitymanager, as when you instance this class and then use it in a
	 * closure you will end up trying to access a closed entitymanager
	 * 
	 * @return
	 */
	protected EntityManager getEntityManager()
	{
		final EntityManager em = EntityManagerProvider.getEntityManager();
		Preconditions.checkNotNull(em, "Entity manager has not been initialized, "
				+ "if you are using a worker thread you will have to call "
				+ "EntityManagerProvider.createEntityManager()");

		Preconditions.checkState(dontUseThis == em,
				"The entity manager has changed since this class was instanced, this is very bad. "
						+ "This class should be instanced and used strickly within the scope of a "
						+ "single request/entitymanager");

		Preconditions.checkState(em.isOpen(),
				"The entity manager is closed, this can happen if you instance this class "
						+ "and then use it in a closure when the closure gets called on a "
						+ "separate thread or servlet request");
		return em;
	}

	public Condition<E> and(final Condition<E> c1)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.and(c1.getPredicates());
			}
		};
	}

	public Condition<E> and(final Condition<E> c1, final Condition<E> c2)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.and(c1.getPredicates(), c2.getPredicates());
			}
		};
	}

	public <L> Condition<E> equal(final SingularAttribute<E, L> field, final L value)
	{

		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(root.get(field), value);
			}
		};
	}

	public <L> Condition<E> equal(final ListAttribute<E, L> field, final L value)
	{

		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(root.get(field), value);
			}
		};
	}

	public <L> Condition<E> equal(final SetAttribute<E, L> field, final L value)
	{

		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(root.get(field), value);
			}
		};
	}

	public <J, V> Condition<E> equal(final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.equal(join.get(field), value);
			}
		};
	}

	public <J, V> Condition<E> equal(final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.equal(join.get(field), value);
			}
		};
	}

	public <J, V> Condition<E> equal(final SetAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.equal(join.get(field), value);
			}
		};
	}

	public <J, V> Condition<E> eq(final JoinBuilder<E, J> join, final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(join.getJoin(root).get(field), value);
			}
		};
	}

	public <J> Condition<E> lessThanOrEqualTo(final JoinBuilder<E, J> join, final SingularAttribute<J, Date> field,
			final Date value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.lessThanOrEqualTo(join.getJoin(root).get(field), value);
			}
		};
	}

	public <J> Condition<E> greaterThanOrEqualTo(final JoinBuilder<E, J> join, final SingularAttribute<J, Date> field,
			final Date value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.greaterThanOrEqualTo(join.getJoin(root).get(field), value);
			}
		};
	}

	/**
	 * specify that JPA should fetch child entities in a single query!
	 * 
	 * @param field
	 * @return
	 */
	public <L> JpaDslAbstract<E, R> fetch(SingularAttribute<E, L> field)
	{
		root.fetch(field, JoinType.LEFT);
		return this;
	}

	public <L> JpaDslAbstract<E, R> fetch(ListAttribute<E, L> field, JoinType type)
	{
		root.fetch(field, type);
		return this;
	}

	public <L> JpaDslAbstract<E, R> fetch(SingularAttribute<E, L> field, JoinType type)
	{
		root.fetch(field, type);
		return this;
	}

	public List<R> getResultList()
	{
		return prepareQuery().getResultList();
	}

	public R getSingleResult()
	{
		limit(1);
		return prepareQuery().getSingleResult();
	}

	public R getSingleResultOrNull()
	{
		limit(1);
		List<R> resultList = prepareQuery().getResultList();
		if (resultList.size() == 0)
			return null;

		return resultList.get(0);
	}

	public <J, V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(
			final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.greaterThanOrEqualTo(join.get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(
			final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.greaterThanOrEqualTo(join.get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(
			final SetAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.greaterThanOrEqualTo(join.get(field), value);
			}
		};
	}

	public <V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(final SingularAttribute<E, V> field,
			final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{

				return builder.greaterThanOrEqualTo(root.get(field), value);
			}
		};
	}

	public <L> Condition<E> isNotNull(final SingularAttribute<E, L> field)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.isNotNull(root.get(field));
			}
		};
	}

	public <L> Condition<E> isNull(final SingularAttribute<E, L> field)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.isNull(root.get(field));
			}
		};
	}

	public <J> Condition<E> joinLike(final SingularAttribute<E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, String> field, final String value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.like(join.get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> lessThan(
			final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.lessThan(join.get(field), value);
			}
		};
	}

	public <V extends Comparable<? super V>> Condition<E> lessThan(final SingularAttribute<? super E, V> field,
			final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.lessThan(root.get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(
			final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.lessThanOrEqualTo(join.get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(
			final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.lessThanOrEqualTo(join.get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(
			final SetAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.lessThanOrEqualTo(join.get(field), value);
			}
		};
	}

	public <V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(final SingularAttribute<E, V> field,
			final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{

				return builder.lessThanOrEqualTo(root.get(field), value);
			}
		};
	}

	public Condition<E> like(final SingularAttribute<? super E, String> field, final String value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{

				return builder.like(root.get(field), value);
			}
		};
	}

	public JpaDslAbstract<E, R> limit(int limit)
	{
		this.limit = limit;
		return this;
	}

	public <L> Condition<E> notEqual(final SingularAttribute<E, L> field, final L value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.notEqual(root.get(field), value);
			}
		};
	}

	public <J, L> Condition<E> notEqual(final JoinBuilder<E, J> join, final SingularAttribute<J, L> field, final L value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.notEqual(getJoin(join).get(field), value);
			}
		};
	}

	public Condition<E> or(final Condition<E> c1, final Condition<E> c2)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.or(c1.getPredicates(), c2.getPredicates());
			}
		};
	}

	List<Order> orders = new LinkedList<>();
	protected Root<E> root;

	public JpaDslAbstract<E, R> orderBy(SingularAttribute<E, ?> field, boolean asc)
	{
		if (asc)
		{
			orders.add(builder.asc(root.get(field)));
		}
		else
		{
			orders.add(builder.desc(root.get(field)));
		}

		return this;
	}

	public <K, V> JpaDslAbstract<E, R> orderBy(JoinBuilder<E, K> join, SingularAttribute<K, V> field, boolean asc)
	{
		if (asc)
		{
			orders.add(builder.asc(getJoin(join).get(field)));
		}
		else
		{
			orders.add(builder.desc(getJoin(join).get(field)));
		}

		return this;
	}

	TypedQuery<R> prepareQuery()
	{
		if (predicate != null)
		{
			criteria.where(predicate);
		}
		if (orders.size() > 0)
		{
			criteria.orderBy(orders);
		}
		TypedQuery<R> query = getEntityManager().createQuery(criteria);

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

	/**
	 * WARNING, order will not be honoured by this method
	 * 
	 * @return
	 */
	public int delete()
	{
		Preconditions.checkArgument(orders.size() == 0, "Order is not supported for delete");
		CriteriaDelete<E> deleteCriteria = builder.createCriteriaDelete(entityClass);
		root = deleteCriteria.getRoot();
		if (predicate != null)
		{
			deleteCriteria.where(predicate);
		}
		Query query = getEntityManager().createQuery(deleteCriteria);

		if (limit != null)
		{
			query.setMaxResults(limit);
		}
		if (startPosition != null)
		{
			query.setFirstResult(startPosition);
		}
		int result = query.executeUpdate();
		getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);

		return result;
	}

	/**
	 * WARNING, order will not be honoured by this method
	 * 
	 * @param attribute
	 * @param value
	 * 
	 * @return
	 */
	public <F> int update(SingularAttribute<E, F> attribute, F value)
	{
		Preconditions.checkArgument(orders.size() == 0, "Order is not supported for delete");
		CriteriaUpdate<E> updateCriteria = builder.createCriteriaUpdate(entityClass);
		root = updateCriteria.getRoot();
		if (predicate != null)
		{
			updateCriteria.where(predicate);
			updateCriteria.set(attribute, value);
		}
		Query query = getEntityManager().createQuery(updateCriteria);

		if (limit != null)
		{
			query.setMaxResults(limit);
		}
		if (startPosition != null)
		{
			query.setFirstResult(startPosition);
		}
		int result = query.executeUpdate();
		getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);

		return result;
	}

	/**
	 * WARNING, order will not be honoured by this method
	 * 
	 * @param attribute
	 * @param value
	 * 
	 * @return
	 */
	public <F extends Object> int update(Map<SingularAttribute<E, F>, F> updatemap)
	{
		Preconditions.checkArgument(orders.size() == 0, "Order is not supported for delete");
		CriteriaUpdate<E> updateCriteria = builder.createCriteriaUpdate(entityClass);
		root = updateCriteria.getRoot();
		if (predicate != null)
		{
			updateCriteria.where(predicate);
			for (Entry<SingularAttribute<E, F>, F> update : updatemap.entrySet())
			{
				updateCriteria.set(update.getKey(), update.getValue());
			}

		}
		Query query = getEntityManager().createQuery(updateCriteria);

		if (limit != null)
		{
			query.setMaxResults(limit);
		}
		if (startPosition != null)
		{
			query.setFirstResult(startPosition);
		}
		int result = query.executeUpdate();
		getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);

		return result;
	}

	public Long count()
	{
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		if (predicate != null)
		{
			query.where(predicate);
		}
		query.select(builder.count(root));

		return getEntityManager().createQuery(query).getSingleResult();
	}

	public Long countDistinct()
	{
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		if (predicate != null)
		{
			query.where(predicate);
		}
		query.select(builder.countDistinct(query.from(entityClass)));

		return getEntityManager().createQuery(query).getSingleResult();
	}

	public JpaDslAbstract<E, R> startPosition(int startPosition)
	{
		this.startPosition = startPosition;
		return this;
	}

	public <K> JoinBuilder<E, K> join(final SingularAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<E, K>(attribute, JoinType.INNER);
	}

	public <K> JoinBuilder<E, K> join(final ListAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<E, K>(attribute, JoinType.INNER);
	}

	public <K> JoinBuilder<E, K> join(final SetAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<E, K>(attribute, JoinType.INNER);
	}

	public <K> JoinBuilder<E, K> join(final SingularAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<E, K>(attribute, type);
	}

	public <K> JoinBuilder<E, K> leftJoin(final SingularAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<E, K>(attribute, JoinType.LEFT);
	}

	public <K> JoinBuilder<E, K> join(final ListAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<E, K>(attribute, type);
	}

	public <K> JoinBuilder<E, K> join(final SetAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<E, K>(attribute, type);
	}

	public JpaDslAbstract<E, R> where(Condition<E> condition)
	{
		predicate = condition.getPredicates();
		return this;
	}

	public abstract class AbstractCondition<Z> implements Condition<Z>
	{
		public Condition<Z> and(final Condition<Z> c1)
		{
			return new AbstractCondition<Z>()
			{

				@Override
				public Predicate getPredicates()
				{
					return builder.and(AbstractCondition.this.getPredicates(), c1.getPredicates());
				}
			};
		}

		public Condition<Z> or(final Condition<Z> c1)
		{
			return new AbstractCondition<Z>()
			{

				@Override
				public Predicate getPredicates()
				{
					return builder.or(AbstractCondition.this.getPredicates(), c1.getPredicates());
				}
			};
		}
	}

	public <V, K> Condition<E> in(final JoinBuilder<E, V> join, final SingularAttribute<V, K> attribute,
			final Collection<K> values)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return getJoin(join).get(attribute).in(values);
			}
		};
	}

	public <V> Condition<E> in(final SingularAttribute<E, V> attribute, final Collection<V> values)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return root.get(attribute).in(values);
			}
		};
	}

	public <V> Condition<E> in(final SetAttribute<E, V> agents, final V agent)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return root.get(agents).in(agent);
			}
		};
	}

	public Condition<E> gtEq(SingularAttribute<E, Date> field, Date value)
	{
		return greaterThanOrEqualTo(field, value);
	}

	public Condition<E> ltEq(SingularAttribute<E, Date> field, Date value)
	{
		return lessThanOrEqualTo(field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> ltEq(final SetAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> gtEq(final ListAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return greaterThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> ltEq(final ListAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> gtEq(final SetAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return greaterThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> ltEq(final SingularAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> gtEq(final SingularAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return greaterThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public Condition<E> lt(SingularAttribute<? super E, Date> field, Date value)
	{
		return lessThan(field, value);
	}

	public <J> Condition<E> eq(SingularAttribute<E, J> field, J value)
	{
		return equal(field, value);
	}

	public <J> Condition<E> eq(ListAttribute<E, J> field, J value)
	{
		return equal(field, value);
	}

	public <J> Condition<E> eq(SetAttribute<E, J> field, J value)
	{
		return equal(field, value);
	}

	public <J, V> Condition<E> eq(final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return equal(joinAttribute, joinType, field, value);
	}

	public <J, V> Condition<E> eq(final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return equal(joinAttribute, joinType, field, value);
	}

	public <J, V> Condition<E> eq(final SetAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return equal(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> lt(final SingularAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThan(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> between(final SingularAttribute<? super E, V> field,
			final V start, final V end)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.between(root.get(field), start, end);
			}
		};
	}

	public <V extends Comparable<? super V>> Condition<E> between(final JoinBuilder<E, ? super V> joinBuilder,
			final SingularAttribute<? super V, Date> field, final Date start, final Date end)
	{
		return new AbstractCondition<E>()
		{

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Predicate getPredicates()
			{
				return builder.between(getJoin(joinBuilder).get((SingularAttribute) field), start, end);
			}
		};
	}

	Map<JoinBuilder<E, ?>, Join<E, ?>> joins2 = new HashMap<>();

	boolean isJpaContainerDelegate;

	@SuppressWarnings("unchecked")
	<K> Join<E, K> getJoin(JoinBuilder<E, K> builder)
	{
		Join<E, K> join = (Join<E, K>) joins2.get(builder);
		if (join == null)
		{
			join = builder.getJoin(root);
			joins2.put(builder, join);
		}
		return join;
	}

	private <J> Join<E, J> getJoin(SingularAttribute<? super E, J> joinAttribute, JoinType joinType)
	{
		JoinBuilder<E, J> jb = join(joinAttribute, joinType);
		return getJoin(jb);
	}

	private <J> Join<E, J> getJoin(ListAttribute<? super E, J> joinAttribute, JoinType joinType)
	{
		JoinBuilder<E, J> jb = join(joinAttribute, joinType);
		return getJoin(jb);
	}

	private <J> Join<E, J> getJoin(SetAttribute<? super E, J> joinAttribute, JoinType joinType)
	{
		JoinBuilder<E, J> jb = join(joinAttribute, joinType);
		return getJoin(jb);
	}

	public <V> Condition<E> like(final JoinBuilder<E, V> join, final SingularAttribute<V, String> attribute,
			final String pattern)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.like(getJoin(join).get(attribute), pattern);
			}
		};
	}

	/**
	 * for use with vaadin JPAContainer queryDelegate
	 * 
	 * @param criteriaBuilder
	 * @param query
	 * @param predicates
	 */
	public void filtersWillBeAdded(List<Predicate> predicates)
	{
		Preconditions.checkArgument(isJpaContainerDelegate, "You must call isJpaContainerDelegate first!");
		// the query wouldn't be built with the vaadinContainer's query object
		// if you didn't call isJpaContainerDelegate.
		if (predicate != null)
		{
			predicates.add(predicate);
		}
	}

	public <J> AbstractCondition<E> exists(final JpaDslSubqueryBuilder<E, J> subquery)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.exists(subquery.getSubQuery());
			}
		};
	}

	public <J> AbstractCondition<E> not(final Condition<E> condition)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.not(condition.getPredicates());
			}
		};
	}

	public <J> AbstractCondition<E> notExists(final JpaDslSubqueryBuilder<E, J> subquery)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.not(builder.exists(subquery.getSubQuery()));
			}
		};
	}

	public <J> JpaDslSubqueryBuilder<E, J> subQuery(Class<J> target)
	{
		return new JpaDslSubqueryBuilder<E, J>(target, criteria, root);
	}

	public JpaDslAbstract<E, R> distinct()
	{
		criteria.distinct(true);
		return this;
	}

	public Expression<String> trim(final SingularAttribute<E, String> attribute)
	{
		return builder.trim(root.get(attribute));
	}

	public <K> Expression<String> trim(JoinBuilder<E, K> leftJoin, SingularAttribute<K, String> attribute)
	{
		return builder.trim(leftJoin.getJoin(root).get(attribute));
	}

	public Expression<String> concat(Expression<String> trim, String string)
	{
		return builder.concat(trim, string);
	}

	public Expression<String> asString(SingularAttribute<E, ?> field)
	{
		return root.get(field).as(String.class);
	}

	public Expression<String> concat(Expression<String> concat, Expression<String> trim)
	{
		return builder.concat(concat, trim);
	}

	public Condition<E> like(final Expression<String> concat, final String value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.like(concat, value);
			}
		};
	}

	public Condition<E> isNull(final Condition<E> condition)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.isNull(condition.getPredicates());
			}
		};
	}

	public Condition<E> isEmptyString(final SingularAttribute<E, String> attribute)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.or(builder.isNull(root.get(attribute)),
						builder.equal(builder.length(root.get(attribute)), 0));
			}
		};
	}

	public <V extends Comparable<? super V>> Condition<E> greaterThan(final SingularAttribute<E, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.greaterThan(root.get(field), value);
			}
		};
	}

	public <T extends Number> Expression<T> sum(final SingularAttribute<E, T> attribute)
	{
		return builder.sum(root.get(attribute));
	}
	
	public <T> Path<T> get(final SingularAttribute<E, T> attribute)
	{
		return root.get(attribute);
	}
	
	public <K, T> Path<T> get(final JoinBuilder<E, K> join, final SingularAttribute<K, T> attribute)
	{
		return join.getJoin(root).get(attribute);
	}

	public Expression<Number> divide(final Path<? extends Number> path1, final Path<? extends Number> path2)
	{
		return builder.quot(path1, path2);
	}

	public <T extends Number> Expression<Number> divide(final SingularAttribute<E, T> attribute, final Path<? extends Number> path2)
	{
		return builder.quot(get(attribute), path2);
	}
	
	public <T extends Number> Expression<T> max(final SingularAttribute<E, T> attribute)
	{
		return builder.max(root.get(attribute));
	}
}
