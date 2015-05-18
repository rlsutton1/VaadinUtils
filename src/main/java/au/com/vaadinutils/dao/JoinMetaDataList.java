package au.com.vaadinutils.dao;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;

public class JoinMetaDataList<E, K> implements JoinMetaData<E, K>
{

	public JoinMetaDataList(ListAttribute<E, K> attribute2, JoinType type2)
	{
		attribute = attribute2;
		type = type2;
	}

	final ListAttribute<E, K> attribute;
	final JoinType type;

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj)
	{
		JoinMetaDataList other = (JoinMetaDataList) obj;
		return type == other.type && attribute == other.attribute;
	}

	@Override
	public int hashCode()
	{
		return type.hashCode() + attribute.hashCode();
	}

	@Override
	public Join<E, K> getJoin(Root<E> root)
	{
		return root.join(attribute, type);
	}

	@Override
	public Join<E, K> getJoin(Join<?, E> join)
	{
		return join.join(attribute, type);
	}

}