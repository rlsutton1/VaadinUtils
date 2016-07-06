package au.com.vaadinutils.dao;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SetAttribute;

public class JoinMetaDataSet<E, K> implements JoinMetaData<E, K>
{

	public JoinMetaDataSet(SetAttribute<E, K> attribute2, JoinType type2)
	{
		attribute = attribute2;
		type = type2;
	}

	final SetAttribute<E, K> attribute;
	final JoinType type;

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		JoinMetaDataSet other = (JoinMetaDataSet) obj;
		return type == other.type && attribute == other.attribute;
	}

	@Override
	public int hashCode()
	{
		return type.hashCode() + attribute.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Join<E, K> getJoin(Root<E> root)
	{
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