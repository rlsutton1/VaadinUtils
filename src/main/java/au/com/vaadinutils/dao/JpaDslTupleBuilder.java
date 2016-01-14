package au.com.vaadinutils.dao;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;

public class JpaDslTupleBuilder<E> extends JpaDslAbstract<E>
{
	private List<Selection<?>> multiselects = new LinkedList<>();

	JpaDslTupleBuilder(Class<E> entityClass)
	{
		this.entityClass = entityClass;
		builder = getEntityManager().getCriteriaBuilder();

		criteria = builder.createTupleQuery();
		root = criteria.from(entityClass);
	}

	public <T> Path<T> multiselect(SingularAttribute<E, T> attribute)
	{
		final Path<T> path = root.get(attribute);
		multiselects.add(path);
		return path;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Tuple> getResultList()
	{
		criteria.multiselect(multiselects);
		TypedQuery<?> query = prepareQuery();
		return (List<Tuple>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getSingleResult()
	{
		limit(1);
		TypedQuery<?> query = prepareQuery();

		return (E) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getSingleResultOrNull()
	{
		limit(1);
		TypedQuery<?> query = prepareQuery();

		List<?> resultList = query.getResultList();
		if (resultList.size() == 0)
			return null;

		return (E) resultList.get(0);
	}
}
