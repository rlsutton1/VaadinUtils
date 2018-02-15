package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.jpa.JpaQuery;

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

	Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	public abstract class AbstractCondition<Z> implements Condition<Z>
	{
		@Override
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

		@Override
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

	public static final class TypedPath<E, V>
	{
		final Path<V> path;

		public TypedPath(Path<V> path2)
		{
			this.path = path2;
		}
	}

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

	List<Order> orders = new LinkedList<>();

	protected Root<E> root;

	Map<JoinBuilder<E, ?>, Join<E, ?>> joins2 = new HashMap<>();

	boolean isJpaContainerDelegate;

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

	@SuppressWarnings("unchecked")
	public Condition<E> and(final Condition<E>... conditions)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				final List<Predicate> predicates = new ArrayList<>(conditions.length);
				for (Condition<E> condition : conditions)
				{
					predicates.add(condition.getPredicates());
				}

				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}

	public Condition<E> and(final List<Condition<E>> conditions)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				final List<Predicate> predicates = new ArrayList<>(conditions.size());
				for (Condition<E> condition : conditions)
				{
					predicates.add(condition.getPredicates());
				}

				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
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

	public <K> Expression<String> asString(final JoinBuilder<E, K> join, SingularAttribute<K, ?> field)
	{
		return getJoin(join).get(field).as(String.class);
	}

	public <T> Expression<T> asExpression(SingularAttribute<E, T> field)
	{
		return root.get(field).as(field.getJavaType());
	}

	public <T> Expression<?> date(SingularAttribute<E, T> callbackdate)
	{
		return builder.function("date", Integer.class, asExpression(callbackdate));
	}

	public <T> Expression<Integer> hour(SingularAttribute<E, T> callbackdate)
	{
		return builder.function("hour", Integer.class, asExpression(callbackdate));
	}

	public <T> Expression<Integer> minute(SingularAttribute<E, T> callbackdate)
	{
		return builder.function("minute", Integer.class, asExpression(callbackdate));
	}

	public Expression<String> asString(SingularAttribute<E, ?> field)
	{
		return root.get(field).as(String.class);
	}

	public <V extends Comparable<? super V>> Condition<E> between(final JoinBuilder<E, ? super V> joinBuilder,
			final SingularAttribute<? super V, Date> field, final Date start, final Date end)
	{
		return new AbstractCondition<E>()
		{

			@SuppressWarnings(
			{ "unchecked", "rawtypes" })
			@Override
			public Predicate getPredicates()
			{
				return builder.between(getJoin(joinBuilder).get((SingularAttribute) field), start, end);
			}
		};
	}

	public <V extends Comparable<? super V>> Condition<E> between(final JoinBuilder<E, ? super V> joinBuilder,
			final SingularAttribute<? super V, Long> field, final Long start, final Long end)
	{
		return new AbstractCondition<E>()
		{

			@SuppressWarnings(
			{ "unchecked", "rawtypes" })
			@Override
			public Predicate getPredicates()
			{
				return builder.between(getJoin(joinBuilder).get((SingularAttribute) field), start, end);
			}
		};
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

	public <T> Expression<T> coalesce(final SingularAttribute<E, T> attribute1,
			final SingularAttribute<E, T> attribute2)
	{
		return builder.coalesce(root.get(attribute1), root.get(attribute2));
	}

	public Expression<String> concat(Expression<String> concat, Expression<String> trim)
	{
		return builder.concat(concat, trim);
	}

	public Expression<String> concat(Expression<String> trim, String string)
	{
		return builder.concat(trim, string);
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

	public <K, T> Expression<Long> count(final JoinBuilder<E, K> join, final SingularAttribute<K, T> attribute)
	{
		return builder.count(getJoin(join).get(attribute));
	}

	public <K, T> Expression<Long> count(final SingularAttribute<E, T> attribute)
	{
		return builder.count(root.get(attribute));
	}

	public Long countDistinct()
	{
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		if (predicate != null)
		{
			query.where(predicate);
		}
		query.select(builder.countDistinct(root));

		return getEntityManager().createQuery(query).getSingleResult();
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

	public JpaDslAbstract<E, R> distinct()
	{
		criteria.distinct(true);
		return this;
	}

	public Expression<Number> divide(final Path<? extends Number> path1, final Path<? extends Number> path2)
	{
		return builder.quot(path1, path2);
	}

	public Expression<Number> divide(final Expression<? extends Number> path1, int number)
	{
		return builder.quot(path1, number);
	}

	public Expression<Integer> mod(final Expression<Integer> path1, int i)
	{
		return builder.mod(path1, i);
	}

	public Expression<?> toInteger(Expression<Number> expression)
	{
		return builder.toInteger(expression);
	}

	public Expression<Number> divide(final Expression<? extends Number> path1, final Path<? extends Number> path2)
	{
		return builder.quot(path1, path2);
	}

	public <T extends Number> Expression<Number> divide(final SingularAttribute<E, T> attribute,
			final Path<? extends Number> path2)
	{
		return builder.quot(get(attribute), path2);
	}

	public Condition<E> eq(final Expression<String> expression, final String value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(expression, value);
			}
		};
	}

	public <K> Condition<E> eq(final Expression<K> expression, final K value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(expression, value);
			}
		};
	}

	public <K> Condition<E> notEqual(final Expression<K> expression, final int value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.notEqual(expression, value);
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
				return builder.equal(getJoin(join).get(field), value);
			}
		};
	}

	public <J, V> Condition<E> eq(final JoinBuilder<E, J> join, final ListAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(getJoin(join).get(field), value);
			}
		};
	}

	public <J, V> Condition<E> eq(final JoinBuilder<E, J> join, final SetAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(getJoin(join).get(field), value);
			}
		};
	}

	public <J, V> Condition<E> eq(final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return equal(joinAttribute, joinType, field, value);
	}

	public <J> Condition<E> eq(ListAttribute<E, J> field, J value)
	{
		return equal(field, value);
	}

	public <J, V> Condition<E> eq(final SetAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return equal(joinAttribute, joinType, field, value);
	}

	public <J> Condition<E> eq(SetAttribute<E, J> field, J value)
	{
		return equal(field, value);
	}

	public <J> Condition<E> eq(SingularAttribute<? super E, J> field, J value)
	{
		return equal(field, value);
	}

	public <J, V> Condition<E> eq(final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final V value)
	{
		return equal(joinAttribute, joinType, field, value);
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

	public <L> Condition<E> equal(final SingularAttribute<? super E, L> field, final L value)
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

	public <L> JpaDslAbstract<E, R> fetch(ListAttribute<E, L> field, JoinType type)
	{
		root.fetch(field, type);
		return this;
	}

	public <L> JpaDslAbstract<E, R> fetch(SetAttribute<E, L> field, JoinType type)
	{
		root.fetch(field, type);
		return this;
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

	public <L> JpaDslAbstract<E, R> fetch(SingularAttribute<E, L> field, JoinType type)
	{
		root.fetch(field, type);
		return this;
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

	public <K, T> Path<T> get(final JoinBuilder<E, K> join, final SingularAttribute<K, T> attribute)
	{
		return getJoin(join).get(attribute);
	}

	public <T> Path<T> get(final SingularAttribute<E, T> attribute)
	{
		return root.get(attribute);
	}

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
		Preconditions.checkNotNull(em,
				"Entity manager has not been initialized, " + "if you are using a worker thread you will have to call "
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

	@SuppressWarnings("unchecked")
	protected <K> Join<E, K> getJoin(JoinBuilder<E, K> joinBuilder)
	{
		Join<E, K> join = (Join<E, K>) joins2.get(joinBuilder);
		if (join == null)
		{
			join = joinBuilder.getJoin(root, builder);
			joins2.put(joinBuilder, join);
		}
		return join;
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

	private <J> Join<E, J> getJoin(SingularAttribute<? super E, J> joinAttribute, JoinType joinType)
	{
		JoinBuilder<E, J> jb = join(joinAttribute, joinType);
		return getJoin(jb);
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

	public R getSingleResultOrNull(boolean dump)
	{
		limit(1);
		TypedQuery<R> prepareQuery = prepareQuery();
		List<R> resultList = prepareQuery.getResultList();
		if (dump)
		{
			logger.warn(prepareQuery.unwrap(JpaQuery.class).getDatabaseQuery().getSQLString());
		}
		if (resultList.size() == 0)
		{
			return null;
		}

		return resultList.get(0);
	}

	/**
	 * there is also a method to dump the query out at warning level see
	 * getSingleResultOrNull(boolean dump);
	 * 
	 * @return the first matching entity;
	 */
	public R getSingleResultOrNull()
	{
		return getSingleResultOrNull(false);
	}

	public <V extends Comparable<? super V>> Condition<E> greaterThan(final SingularAttribute<E, V> field,
			final V value)
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

	public <J, V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(final JoinBuilder<E, J> join,
			final SingularAttribute<J, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.greaterThanOrEqualTo(getJoin(join).get(field), value);
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(final JoinBuilder<E, J> join,
			final SingularAttribute<J, V> field, final SingularAttribute<E, V> value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.greaterThanOrEqualTo(getJoin(join).get(field), root.get(value));
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> lessThan(final JoinBuilder<E, J> join,
			final SingularAttribute<J, V> field, final SingularAttribute<E, V> value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.lessThan(getJoin(join).get(field), root.get(value));
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(final JoinBuilder<E, J> join,
			final SingularAttribute<J, V> field, final SingularAttribute<E, V> value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.lessThanOrEqualTo(getJoin(join).get(field), root.get(value));
			}
		};
	}

	public <J, K, V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(final JoinBuilder<E, J> join,
			final SingularAttribute<J, V> field, final JoinBuilder<E, K> join2, final SingularAttribute<K, V> field2)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.lessThanOrEqualTo(getJoin(join).get(field), getJoin(join2).get(field2));
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

	public <J, V extends Comparable<? super V>> Condition<E> greaterThan(
			final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final SingularAttribute<E, V> value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.greaterThan(join.get(field), root.get(value));
			}
		};
	}

	public <J, V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(
			final ListAttribute<? super E, J> joinAttribute, final JoinType joinType,
			final SingularAttribute<J, V> field, final SingularAttribute<E, V> value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				Join<E, J> join = getJoin(joinAttribute, joinType);
				return builder.greaterThanOrEqualTo(join.get(field), root.get(value));
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

	public <V extends Comparable<? super V>> Condition<E> greaterThanOrEqualTo(final TypedPath<E, V> field,
			final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{

				return builder.greaterThanOrEqualTo(field.path, value);
			}
		};
	}

	public JpaDslAbstract<E, R> groupBy(Expression<?>... expressions)
	{
		criteria.groupBy(expressions);
		return this;
	}

	public <V> Condition<E> gtEq(final JoinBuilder<E, V> joinBuilder, final SingularAttribute<V, Date> field,
			final Date value)
	{
		return greaterThanOrEqualTo(joinBuilder, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> gtEq(final ListAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return greaterThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> gtEq(final SetAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return greaterThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> gtEq(final SingularAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return greaterThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public Condition<E> gtEq(SingularAttribute<E, Date> field, Date value)
	{
		return greaterThanOrEqualTo(field, value);
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

	public <V, K> Condition<E> in(final JoinBuilder<E, V> join, final ListAttribute<V, K> attribute,
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

	public <V> Condition<E> in(final SetAttribute<E, V> attribute, final Collection<V> values)
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

	public <V> Condition<E> in(final SingularAttribute<E, V> attribute, final Collection<V> values)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				if (values.isEmpty())
				{
					logger.warn("Empty set supplied for IN clause on attribute " + attribute.getJavaType() + " "
							+ attribute.getName());
					return builder.isFalse(builder.literal(true));
				}
				return root.get(attribute).in(values);
			}
		};
	}

	public <V> Condition<E> in(final SingularAttribute<E, V> attribute, final V[] values)
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

	public Condition<E> isNotEmptyString(final SingularAttribute<E, String> attribute)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.and(builder.isNotNull(root.get(attribute)),
						builder.notEqual(builder.length(root.get(attribute)), 0));
			}
		};
	}

	public <L> Condition<E> isFalse(final JoinBuilder<E, L> join, final SingularAttribute<L, Boolean> field)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				return builder.isFalse(getJoin(join).get(field));
			}
		};
	}

	public Condition<E> isFalse(final SingularAttribute<E, Boolean> field)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				return builder.isFalse(root.get(field));
			}
		};
	}

	public <L, J> Condition<E> isNotNull(final JoinBuilder<E, L> join, final SingularAttribute<L, J> field)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.isNotNull(getJoin(join).get(field));
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

	public <L, J> Condition<E> isNull(final JoinBuilder<E, L> join, final SingularAttribute<L, J> field)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				return builder.isNull(getJoin(join).get(field));
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

	public <K> Condition<E> isNull(final TypedPath<E, K> path)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.isNull(path.path);
			}
		};
	}

	public <L> Condition<E> isTrue(final JoinBuilder<E, L> join, final SingularAttribute<L, Boolean> field)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				return builder.isTrue(getJoin(join).get(field));
			}
		};
	}

	public Condition<E> isTrue(final SingularAttribute<E, Boolean> field)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				return builder.isTrue(root.get(field));
			}
		};
	}

	public <K> JoinBuilder<E, K> join(final ListAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<>(attribute, JoinType.INNER, false);
	}

	public <K> JoinBuilder<E, K> join(final ListAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<>(attribute, type, false);
	}

	public <K> JoinBuilder<E, K> join(final SetAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<>(attribute, JoinType.INNER, false);
	}

	public <K> JoinBuilder<E, K> join(final SetAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<>(attribute, type, false);
	}

	public <K> JoinBuilder<E, K> join(final SingularAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<>(attribute, JoinType.INNER, false);
	}

	public <K> JoinBuilder<E, K> join(final SingularAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<>(attribute, type, false);
	}

	public <K> JoinBuilder<E, K> joinFetch(final ListAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<>(attribute, type, true);
	}

	public <K> JoinBuilder<E, K> joinFetch(final SetAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<>(attribute, type, true);
	}

	public <K> JoinBuilder<E, K> joinFetch(final SingularAttribute<? super E, K> attribute, JoinType type)
	{
		return new JoinBuilder<>(attribute, type, true);
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

	public <K> JoinBuilder<E, K> leftJoin(final SingularAttribute<? super E, K> attribute)
	{
		return new JoinBuilder<>(attribute, JoinType.LEFT, false);
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

	public <J> Condition<E> lessThanOrEqualTo(final JoinBuilder<E, J> join, final SingularAttribute<J, Date> field,
			final Date value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.lessThanOrEqualTo(getJoin(join).get(field), value);
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

	public <V extends Comparable<? super V>> Condition<E> lessThanOrEqualTo(final TypedPath<E, V> field, final V value)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{

				return builder.lessThanOrEqualTo(field.path, value);
			}
		};
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

	public Condition<E> lt(SingularAttribute<? super E, Date> field, Date value)
	{
		return lessThan(field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> lt(final SingularAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThan(joinAttribute, joinType, field, value);
	}

	public <V> Condition<E> ltEq(final JoinBuilder<E, V> joinBuilder, final SingularAttribute<V, Date> field,
			final Date value)
	{
		return lessThanOrEqualTo(joinBuilder, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> ltEq(final ListAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> ltEq(final SetAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public <J, V extends Comparable<? super V>> Condition<E> ltEq(final SingularAttribute<? super E, J> joinAttribute,
			final JoinType joinType, final SingularAttribute<J, V> field, final V value)
	{
		return lessThanOrEqualTo(joinAttribute, joinType, field, value);
	}

	public Condition<E> ltEq(SingularAttribute<E, Date> field, Date value)
	{
		return lessThanOrEqualTo(field, value);
	}

	public <T extends Number> Expression<T> max(final SingularAttribute<E, T> attribute)
	{
		return builder.max(root.get(attribute));
	}

	public <T extends Comparable<T>> Expression<T> greatest(final SingularAttribute<E, T> attribute)
	{
		return builder.greatest(root.get(attribute));
	}

	public <T extends Comparable<T>> Expression<T> least(final SingularAttribute<E, T> attribute)
	{
		return builder.least(root.get(attribute));
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

	public <J, L> Condition<E> notEqual(final JoinBuilder<E, J> join, final SingularAttribute<J, L> field,
			final L value)
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

	// Useful when building dynamic queries
	public Condition<E> oneEqualsOne()
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				return builder.equal(builder.literal(1), 1);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public Condition<E> or(final Condition<E>... conditions)
	{
		return new AbstractCondition<E>()
		{
			@Override
			public Predicate getPredicates()
			{
				final List<Predicate> predicates = new ArrayList<>(conditions.length);
				for (Condition<E> condition : conditions)
				{
					predicates.add(condition.getPredicates());
				}

				return builder.or(predicates.toArray(new Predicate[predicates.size()]));
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

	public JpaDslAbstract<E, R> orderBy(SingularAttribute<? super E, ?> field, boolean asc)
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

	public JpaDslAbstract<E, R> orderBy(String field, boolean asc)
	{
		List<String> attributes = Arrays.asList(field.split("\\."));
		Path<Object> path = null;
		for (String attribute : attributes)
		{
			if (path == null)
			{
				path = root.get(attribute);
			}
			else
			{
				path = path.get(attribute);
			}
		}

		if (asc)
		{
			orders.add(builder.asc(path));
		}
		else
		{
			orders.add(builder.desc(path));
		}

		return this;
	}

	public <V, J> TypedPath<E, V> path(SingularAttribute<? super E, J> partA, SingularAttribute<? super J, V> partB)
	{
		return new TypedPath<>(root.get(partA).get(partB));

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

	public Expression<String> replace(final SingularAttribute<E, String> field, final String match,
			final String replacement)
	{
		// creats sql replace(field,match,replacement)
		return builder.function("replace", String.class, root.get(field), builder.literal(match),
				builder.literal(replacement));

	}

	public JpaDslAbstract<E, R> startPosition(int startPosition)
	{
		this.startPosition = startPosition;
		return this;
	}

	public <J> JpaDslSubqueryBuilder<E, J> subQuery(Class<J> target)
	{
		return new JpaDslSubqueryBuilder<>(target, criteria, root);
	}

	public <T extends Number> Expression<T> sum(final SingularAttribute<E, T> attribute)
	{
		return builder.sum(root.get(attribute));
	}

	public <K> Expression<String> trim(JoinBuilder<E, K> join, SingularAttribute<K, String> attribute)
	{
		return builder.trim(getJoin(join).get(attribute));
	}

	public Expression<String> trim(final SingularAttribute<E, String> attribute)
	{
		return builder.trim(root.get(attribute));
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

	public JpaDslAbstract<E, R> where(Condition<E> condition)
	{
		predicate = condition.getPredicates();
		return this;
	}

	@SuppressWarnings("unchecked")
	public JpaDslAbstract<E, R> where(final Condition<E>... conditions)
	{
		final List<Predicate> predicates = new ArrayList<>(conditions.length);
		for (Condition<E> condition : conditions)
		{
			predicates.add(condition.getPredicates());
		}
		predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
		return this;
	}

	public JpaDslAbstract<E, R> where(final List<Condition<E>> conditions)
	{
		final List<Predicate> predicates = new ArrayList<>(conditions.size());
		for (Condition<E> condition : conditions)
		{
			predicates.add(condition.getPredicates());
		}
		predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
		return this;
	}
}
