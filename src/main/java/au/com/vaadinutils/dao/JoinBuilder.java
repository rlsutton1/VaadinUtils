package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.dao.JoinOnBuilder.JoinOnType;

public class JoinBuilder<E, K>
{
	List<JoinOnBuilder<K, ?>> joinOnBuilders = new LinkedList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((joins == null) ? 0 : joins.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof JoinBuilder))
		{
			return false;
		}
		@SuppressWarnings("rawtypes")
		JoinBuilder other = (JoinBuilder) obj;
		if (joins == null)
		{
			if (other.joins != null)
			{
				return false;
			}
		}
		else if (!joins.equals(other.joins))
		{
			return false;
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	List<JoinMetaData> joins = new LinkedList<>();

	private JoinBuilder()
	{
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JoinBuilder(final SetAttribute<? super E, K> attribute, final JoinType type, final boolean fetch)
	{
		joins.add(new JoinMetaDataSet(attribute, type, fetch));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JoinBuilder(final SingularAttribute<? super E, K> attribute, final JoinType type, final boolean fetch)
	{
		joins.add(new JoinMetaDataSingular(attribute, type, fetch));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JoinBuilder(final ListAttribute<? super E, K> attribute, final JoinType type, final boolean fetch)
	{
		joins.add(new JoinMetaDataList(attribute, type, fetch));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Join<E, K> getJoin(Root<E> root, CriteriaBuilder builder)
	{
		Join p = null;
		for (JoinMetaData join : joins)
		{
			if (p == null)
			{
				p = join.getJoin(root);
			}
			else
			{
				p = join.getJoin(p);
			}

			if (!joinOnBuilders.isEmpty())
			{
				p = getOnJoin(p, builder);
			}
		}

		return p;
	}

	public <V> Join<E, K> getOnJoin(Join<E, K> join, CriteriaBuilder builder)
	{
		List<Predicate> predicates = new ArrayList<>(joinOnBuilders.size());
		for (JoinOnBuilder<K, ?> joinOnBuilder : joinOnBuilders)
		{
			switch (joinOnBuilder.getType())
			{
			case EQUAL:
				predicates.add(builder.equal(castGet(joinOnBuilder.getAttribute(), join), joinOnBuilder.getValue()));
				break;
			case IN:
				predicates
						.add(castGet(joinOnBuilder.getAttribute(), join).in((Collection<?>) joinOnBuilder.getValue()));
				break;
			}
		}

		return join.on(builder.and(predicates.toArray(new Predicate[joinOnBuilders.size()])));
	}

	@SuppressWarnings("unchecked")
	private <V> Expression<?> castGet(final Attribute<K, V> attribute, Join<E, K> join)
	{
		if (attribute instanceof SingularAttribute)
		{
			return join.get((SingularAttribute<K, V>) attribute);
		}
		else if (attribute instanceof ListAttribute)
		{
			return join.get((ListAttribute<K, V>) attribute);
		}
		else if (attribute instanceof SetAttribute)
		{
			return join.get((SetAttribute<K, V>) attribute);
		}
		else
		{
			return null;
		}
	}

	public <T, V> JoinBuilder<E, K> onEq(final SingularAttribute<K, V> attribute, final V value)
	{
		JoinBuilder<E, K> jb = new JoinBuilder<E, K>();
		jb.joins.addAll(joins);
		jb.joinOnBuilders.addAll(joinOnBuilders);
		jb.joinOnBuilders.add(new JoinOnBuilder<K, V>(attribute, value, JoinOnType.EQUAL));
		return jb;
	}

	public <T, V> JoinBuilder<E, K> onIn(final SingularAttribute<K, V> attribute, final Collection<V> value)
	{
		JoinBuilder<E, K> jb = new JoinBuilder<E, K>();
		jb.joins.addAll(joins);
		jb.joinOnBuilders.addAll(joinOnBuilders);
		jb.joinOnBuilders.add(new JoinOnBuilder<K, V>(attribute, value, JoinOnType.IN));
		return jb;
	}

	public <T> JoinBuilder<E, T> join(final SingularAttribute<K, T> attribute)
	{
		return join(attribute, JoinType.INNER, false);
	}

	public <T> JoinBuilder<E, T> join(final ListAttribute<K, T> attribute)
	{
		return join(attribute, JoinType.INNER, false);
	}

	public <T> JoinBuilder<E, T> join(final SetAttribute<K, T> attribute)
	{
		return join(attribute, JoinType.INNER, false);
	}

	public <T> JoinBuilder<E, T> join(final ListAttribute<K, T> attribute, final JoinType type)
	{
		return join(attribute, type, false);
	}

	public <T> JoinBuilder<E, T> join(final SetAttribute<K, T> attribute, final JoinType type)
	{
		return join(attribute, type, false);
	}

	public <T> JoinBuilder<E, T> join(final SingularAttribute<K, T> attribute, final JoinType type)
	{
		return join(attribute, type, false);
	}

	public <T> JoinBuilder<E, T> joinFetch(final ListAttribute<K, T> attribute, final JoinType type)
	{
		return join(attribute, type, true);
	}

	public <T> JoinBuilder<E, T> joinFetch(final SetAttribute<K, T> attribute, final JoinType type)
	{
		return join(attribute, type, true);
	}

	public <T> JoinBuilder<E, T> joinFetch(final SingularAttribute<K, T> attribute, final JoinType type)
	{
		return join(attribute, type, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> JoinBuilder<E, T> join(final ListAttribute<K, T> attribute, final JoinType type, final boolean fetch)
	{
		final JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataList(attribute, type, fetch));

		return jb;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> JoinBuilder<E, T> join(final SingularAttribute<K, T> attribute, final JoinType type,
			final boolean fetch)
	{
		final JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataSingular(attribute, type, fetch));

		return jb;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> JoinBuilder<E, T> join(final SetAttribute<K, T> attribute, final JoinType type, final boolean fetch)
	{
		final JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataSet(attribute, type, fetch));

		return jb;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String toString()
	{
		String stringValue = "";
		final int listSize = joins.size();
		for (int i = 0; i < listSize; i++)
		{
			final JoinMetaData join = joins.get(i);
			stringValue += join.toString();
			if ((i + 1) < listSize)
			{
				stringValue += ", ";
			}
		}

		return stringValue;
	}
}
