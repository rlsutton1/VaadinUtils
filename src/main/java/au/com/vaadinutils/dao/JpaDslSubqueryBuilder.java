package au.com.vaadinutils.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.dao.JpaBaseDao.Condition;

public class JpaDslSubqueryBuilder<J, E> extends JpaDslBuilder<E>
{

	private Subquery<E> subQuery;
	private Root<J> parentRoot;

	JpaDslSubqueryBuilder(EntityManager entityManager, Class<E> entityClass, CriteriaQuery<J> query, Root<J> parentRoot)
	{
		super();
		this.entityManager = entityManager;
		this.entityClass = entityClass;
	
		builder = entityManager.getCriteriaBuilder();
		this.parentRoot = parentRoot;

		subQuery = query.subquery(entityClass);
		root = subQuery.from(entityClass);
		subQuery.select(root);
	}

	public <V> AbstractCondition<E> joinParentQuery(final SingularAttribute<J, V> parentAttrib, final SetAttribute<E, V> attrib)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(parentRoot.get(parentAttrib),root.get(attrib));
				
			}
		};
	}
	
	public <V> AbstractCondition<E> joinParentQuery(final SingularAttribute<J, V> parentAttrib, final SingularAttribute<E, V> subQueryAttrib)
	{
		return new AbstractCondition<E>()
		{

			@Override
			public Predicate getPredicates()
			{
				return builder.equal(parentRoot.get(parentAttrib),root.get(subQueryAttrib));
				
			}
		};
	}
	
	/**
	 * something like 
	 * @param join
	 * @param parentAttrib
	 * @param subQueryAttrib
	 * @return
	 */
	public <V> Condition<E> joinParentQuery(final JoinBuilder<E,J> join,
		final SingularAttribute<J, V> parentAttrib, final SingularAttribute<J, V> subQueryAttrib)
	{
	    return new AbstractCondition<E>()
			{

				@Override
				public Predicate getPredicates()
				{
					return builder.equal(parentRoot.get(parentAttrib),join.getJoin(root).get(subQueryAttrib));
					
				}
			};
	}


	public Subquery<E> getSubQuery()
	{
		if (predicate != null)
		{
			subQuery.where(predicate);
		}
		return subQuery;
	}

	@Override
	public int delete()
	{
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public Long count()
	{
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public List<E> getResultList()
	{
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public E getSingleResultOrNull()
	{
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public E getSingleResult()
	{
		throw new RuntimeException("Not Implemented");

	}

}
