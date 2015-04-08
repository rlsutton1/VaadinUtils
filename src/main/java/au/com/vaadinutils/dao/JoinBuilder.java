package au.com.vaadinutils.dao;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class JoinBuilder<  E, K>
{

	

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
		joins.add(new JoinMetaDataPlural(attribute, type));
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
		jb.joins.add(new JoinMetaDataPlural(attribute, type));
		return jb;
	}
}
