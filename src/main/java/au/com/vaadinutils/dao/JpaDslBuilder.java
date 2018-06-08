package au.com.vaadinutils.dao;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class JpaDslBuilder<E> extends JpaDslAbstract<E, E>
{
	public JpaDslBuilder(Class<E> entityClass)
	{
		this.entityClass = entityClass;
		builder = getEntityManager().getCriteriaBuilder();

		criteria = builder.createQuery(entityClass);
		root = criteria.from(entityClass);
		criteria.select(root);
	}

	/**
	 * constructor specifically for the JpaContainerDelegate usage
	 * 
	 * @param query
	 * @param entityClass
	 */
	@SuppressWarnings("unchecked")
	public JpaDslBuilder(CriteriaQuery<E> query, Class<E> entityClass)
	{
		this.entityClass = entityClass;
		builder = getEntityManager().getCriteriaBuilder();

		criteria = query;
		root = (Root<E>) criteria.getRoots().iterator().next();

		isJpaContainerDelegate = true;
	}

	/**
	 * @deprecated This method is broken! You should use a JpaDslCountBuilder to
	 *             get counts.
	 */
	@Deprecated
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

	/**
	 * @deprecated This method is broken! You should use a JpaDslCountBuilder to
	 *             get counts.
	 */
	@Deprecated
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
}
