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

}
