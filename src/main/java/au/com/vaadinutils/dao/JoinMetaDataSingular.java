package au.com.vaadinutils.dao;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public class JoinMetaDataSingular<E, K> implements JoinMetaData<E, K>
{
	final SingularAttribute<E, K> attribute;
	final JoinType type;
	final boolean fetch;

	public JoinMetaDataSingular(final SingularAttribute<E, K> attribute, final JoinType type, final boolean fetch)
	{
		this.attribute = attribute;
		this.type = type;
		this.fetch = fetch;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		JoinMetaDataSingular other = (JoinMetaDataSingular) obj;
		return type == other.type && attribute == other.attribute && fetch == other.fetch;
	}

	@Override
	public int hashCode()
	{
		return type.hashCode() + attribute.hashCode() + new Boolean(fetch).hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Join<E, K> getJoin(Root<E> root)
	{
		for (Fetch<E, ?> join : root.getFetches())
		{
			if (join.getAttribute().equals(attribute) && join.getJoinType().equals(type))
			{
				return (Join<E, K>) join;
			}
		}

		if (fetch)
		{
			return (Join<E, K>) root.fetch(attribute, type);
		}

		for (Join<E, ?> join : root.getJoins())
		{
			if (join.getAttribute().equals(attribute) && join.getJoinType().equals(type))
			{
				return (Join<E, K>) join;
			}
		}

		return root.join(attribute, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Join<E, K> getJoin(Join<?, E> join)
	{
		for (Fetch<E, ?> existingJoin : join.getFetches())
		{
			if (existingJoin.getAttribute().equals(attribute) && existingJoin.getJoinType().equals(type))
			{
				return (Join<E, K>) existingJoin;
			}
		}

		if (fetch)
		{
			return (Join<E, K>) join.fetch(attribute, type);
		}

		for (Join<E, ?> existingJoin : join.getJoins())
		{
			if (existingJoin.getAttribute().equals(attribute) && existingJoin.getJoinType().equals(type))
			{
				return (Join<E, K>) existingJoin;
			}
		}

		return join.join(attribute, type);
	}

	@Override
	public String toString()
	{
		return attribute.getDeclaringType().getJavaType().getSimpleName() + "->" + attribute.getName() + ":"
				+ type.toString();
	}
}