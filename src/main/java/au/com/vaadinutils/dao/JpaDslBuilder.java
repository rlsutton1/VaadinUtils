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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.google.common.base.Preconditions;

public class JpaDslBuilder<E>
{

	final private CriteriaBuilder builder;
	Map<JoinDescriptor<E>, Join<E, ?>> joins = new HashMap<>();

	private Integer limit = null;

	Predicate predicate = null;

	private Integer startPosition = null;
	final CriteriaQuery<E> criteria;
	final private EntityManager entityManager;
	final private Class<E> entityClass;

	JpaDslBuilder(EntityManager entityManager, Class<E> entityClass)
	{
		this.entityManager = entityManager;
		this.entityClass = entityClass;
		builder = entityManager.getCriteriaBuilder();

		criteria = builder.createQuery(entityClass);
		root = criteria.from(entityClass);
		criteria.select(root);

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

	private <J> Join<E, J> getJoin(SingularAttribute<? super E, J> joinAttribute, JoinType joinType)
	{
		JoinDescriptor<E> joinDescriptor = new JoinDescriptor<E>(joinAttribute, joinType);
		@SuppressWarnings("unchecked")
		Join<E, J> join = (Join<E, J>) joins.get(joinDescriptor);
		{
			if (join != null)
			{
				return join;
			}
		}
		join = root.join(joinAttribute, joinType);
		joins.put(joinDescriptor, join);
		return join;
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

	public Condition<E> like(final SingularAttribute<E, String> field, final String value)
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
	private Root<E> root;

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
		Preconditions.checkArgument(orders.size() > 0, "Order is not supported for delete");
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
		return query.executeUpdate();

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

	public JpaDslBuilder<E> where(Condition<E> condition)
	{
		predicate = condition.getPredicates();
		return this;
	}

	public abstract class AbstractCondition<E> implements Condition<E>
	{
		public Condition<E> and(final Condition<E> c1)
		{
			return new AbstractCondition<E>()
			{

				@Override
				public Predicate getPredicates()
				{
					return builder.and(AbstractCondition.this.getPredicates(), c1.getPredicates());
				}
			};
		}

		public Condition<E> or(final Condition<E> c1)
		{
			return new AbstractCondition<E>()
			{

				@Override
				public Predicate getPredicates()
				{
					return builder.or(AbstractCondition.this.getPredicates(), c1.getPredicates());
				}
			};
		}

	}

	public Condition<E> in(final SingularAttribute<E, Long> attribute, final Collection<Long> queueIds)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return root.get(attribute).in(queueIds);
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

	public <J, V> Condition<E> eq(final SingularAttribute<? super E, J> joinAttribute, final JoinType joinType,
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
	
	public interface Condition<E>
	{

		Condition<E> and(Condition<E> c1);

		Predicate getPredicates();

		Condition<E> or(Condition<E> c1);

	}

}
