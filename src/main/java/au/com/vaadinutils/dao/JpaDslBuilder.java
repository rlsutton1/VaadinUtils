package au.com.vaadinutils.dao;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.dao.JpaBaseDao.Condition;

import com.google.common.base.Preconditions;

public class JpaDslBuilder<E>
{

	protected CriteriaBuilder builder;

	private Integer limit = null;

	Predicate predicate = null;

	private Integer startPosition = null;
	private CriteriaQuery<E> criteria;
	protected EntityManager entityManager;
	protected Class<E> entityClass;

	JpaDslBuilder(EntityManager entityManager, Class<E> entityClass)
	{
		this.entityManager = entityManager;
		this.entityClass = entityClass;
		builder = entityManager.getCriteriaBuilder();

		criteria = builder.createQuery(entityClass);
		root = criteria.from(entityClass);
		criteria.select(root);

	}

	protected JpaDslBuilder()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * constructor specifically for the JpaContainerDelegate usage
	 * 
	 * @param query
	 * @param entityClass
	 * @param entityManager
	 */
	@SuppressWarnings("unchecked")
	public JpaDslBuilder(CriteriaQuery<E> query, Class<E> entityClass, EntityManager entityManager)
	{
		this.entityManager = entityManager;
		this.entityClass = entityClass;
		builder = entityManager.getCriteriaBuilder();

		criteria = query;
		root = (Root<E>) criteria.getRoots().iterator().next();

		isJpaContainerDelegate = true;

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

	/**
	 * specify that JPA should fetch child entities in a single query!
	 * 
	 * @param field
	 * @return
	 */
	public <L> JpaDslBuilder<E> fetch(SingularAttribute<E, L> field)
	{
		root.fetch(field, JoinType.LEFT);
		return this;
	}

	public <L> JpaDslBuilder<E> fetch(ListAttribute<E, L> field, JoinType left)
	{
		root.fetch(field, JoinType.LEFT);
		return this;
	}

	public <L> JpaDslBuilder<E> fetch(SingularAttribute<E, L> field, JoinType left)
	{
		root.fetch(field, JoinType.LEFT);
		return this;

	}

	public List<E> getResultList()
	{
		TypedQuery<E> query = prepareQuery();
		return query.getResultList();
	}

	public E getSingleResultOrNull()
	{
		limit(1);
		TypedQuery<E> query = prepareQuery();

		List<E> resultList = query.getResultList();
		if (resultList.size() == 0)
		{
			return null;
		}
		return resultList.get(0);
	}

	public E getSingleResult()
	{
		limit(1);
		TypedQuery<E> query = prepareQuery();

		return query.getSingleResult();
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

	public JpaDslBuilder<E> limit(int limit)
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

	public JpaDslBuilder<E> orderBy(SingularAttribute<E, ?> field, boolean asc)
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

	public <K, V> JpaDslBuilder<E> orderBy(JoinBuilder<E, K> join, SingularAttribute<K, V> field, boolean asc)
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

	private TypedQuery<E> prepareQuery()
	{

		if (predicate != null)
		{
			criteria.where(predicate);
		}
		if (orders.size() > 0)
		{
			criteria.orderBy(orders);
		}
		if (distinct)
		{
			criteria.distinct(true);
		}
		TypedQuery<E> query = entityManager.createQuery(criteria);

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
		Query query = entityManager.createQuery(deleteCriteria);

		if (limit != null)
		{
			query.setMaxResults(limit);
		}
		if (startPosition != null)
		{
			query.setFirstResult(startPosition);
		}
		int result = query.executeUpdate();
		entityManager.getEntityManagerFactory().getCache().evict(entityClass);
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
	public <F> int update(SingularAttribute<E, F> attribute,  F value)
	{
		Preconditions.checkArgument(orders.size() == 0, "Order is not supported for delete");
		CriteriaUpdate<E> updateCriteria = builder.createCriteriaUpdate(entityClass);
		root = updateCriteria.getRoot();
		if (predicate != null)
		{
			updateCriteria.where(predicate);
			updateCriteria.set(attribute, value);
		}
		Query query = entityManager.createQuery(updateCriteria);

		if (limit != null)
		{
			query.setMaxResults(limit);
		}
		if (startPosition != null)
		{
			query.setFirstResult(startPosition);
		}
		int result = query.executeUpdate();
		entityManager.getEntityManagerFactory().getCache().evict(entityClass);
		return result;

	}

	public Long count()
	{
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		if (predicate != null)
		{
			query.where(predicate);
		}
		query.select(builder.count(query.from(entityClass)));

		return entityManager.createQuery(query).getSingleResult();
	}

	public Long countDistinct()
	{
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		if (predicate != null)
		{
			query.where(predicate);
		}
		query.select(builder.countDistinct(query.from(entityClass)));

		return entityManager.createQuery(query).getSingleResult();
	}

	public JpaDslBuilder<E> startPosition(int startPosition)
	{
		this.startPosition = startPosition;
		return this;
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

	public JpaDslBuilder<E> where(Condition<E> condition)
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

	private boolean isJpaContainerDelegate;

	private boolean distinct = false;

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
		return new JpaDslSubqueryBuilder<E, J>(entityManager, target, criteria, root);
	}

	public JpaDslBuilder<E> distinct()
	{
		this.distinct = true;
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

}
