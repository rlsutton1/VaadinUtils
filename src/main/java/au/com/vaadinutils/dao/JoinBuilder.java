package au.com.vaadinutils.dao;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class JoinBuilder<  E, K>
{

	

	/* (non-Javadoc)
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

	/* (non-Javadoc)
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JoinBuilder(SingularAttribute<? super E, K> attribute, JoinType type)
	{
		joins.add(new JoinMetaDataSingular(attribute, type));
	}

	private JoinBuilder()
	{
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JoinBuilder(SetAttribute<? super E, K> attribute, JoinType type)
	{
		joins.add(new JoinMetaDataSet(attribute, type));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JoinBuilder(ListAttribute<? super E, K> attribute, JoinType type)
	{
		joins.add(new JoinMetaDataList(attribute, type));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Join<E, K> getJoin(Root<E> root)
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
		}
		return p;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> JoinBuilder<E, T> join(final SingularAttribute<K, T> attribute)
	{

		JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataSingular(attribute, JoinType.INNER));
		return jb;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> JoinBuilder<E, T> join(final ListAttribute<K, T> attribute)
	{

		JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataList(attribute, JoinType.INNER));
		return jb;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> JoinBuilder<E, T> join(final SetAttribute<K, T> attribute)
	{

		JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataSet(attribute, JoinType.INNER));
		return jb;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> JoinBuilder<E, T> join(final ListAttribute<K, T> attribute, JoinType type)
	{

		JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataList(attribute, type));
		return jb;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> JoinBuilder<E, T> join(final SingularAttribute<K, T> attribute, JoinType type)
	{

		JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataSingular(attribute, type));
		return jb;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> JoinBuilder<E, T> join(final SetAttribute<K, T> attribute, JoinType type)
	{

		JoinBuilder<E, T> jb = new JoinBuilder<E, T>();
		jb.joins.addAll(joins);
		jb.joins.add(new JoinMetaDataSet(attribute, type));
		return jb;
	}
}
