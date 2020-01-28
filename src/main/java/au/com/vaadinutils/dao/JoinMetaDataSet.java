package au.com.vaadinutils.dao;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SetAttribute;

public class JoinMetaDataSet<E, K> implements JoinMetaData<E, K>
{
	final SetAttribute<E, K> attribute;
	final JoinType type;
	final boolean fetch;

	public JoinMetaDataSet(final SetAttribute<E, K> attribute, final JoinType type, final boolean fetch)
	{
		this.attribute = attribute;
		this.type = type;
		this.fetch = fetch;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JoinMetaDataSet other = (JoinMetaDataSet) obj;
		if (attribute == null)
		{
			if (other.attribute != null)
				return false;
		}
		else if (!attribute.equals(other.attribute))
			return false;
		if (fetch != other.fetch)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + (fetch ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
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