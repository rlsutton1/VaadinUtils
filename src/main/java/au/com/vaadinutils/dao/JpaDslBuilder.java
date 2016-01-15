package au.com.vaadinutils.dao;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class JpaDslBuilder<E> extends JpaDslAbstract<E>
{
	@SuppressWarnings("unchecked")
	JpaDslBuilder(Class<E> entityClass)
	{
		this.entityClass = entityClass;
		builder = getEntityManager().getCriteriaBuilder();

		criteria = builder.createQuery(entityClass);
		root = criteria.from(entityClass);
		((CriteriaQuery<E>) criteria).select(root);
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

	@SuppressWarnings("unchecked")
	@Override
	public List<E> getResultList()
	{
		TypedQuery<?> query = prepareQuery();
		return (List<E>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public E getSingleResult()
	{
		limit(1);
		TypedQuery<?> query = prepareQuery();

		return (E) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public E getSingleResultOrNull()
	{
		limit(1);
		TypedQuery<?> query = prepareQuery();

		List<?> resultList = query.getResultList();
		if (resultList.size() == 0)
			return null;

		return (E) resultList.get(0);
	}
}
