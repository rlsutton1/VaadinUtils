package au.com.vaadinutils.dao;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;

public class JpaDslTupleBuilder<E> extends JpaDslAbstract<E, Tuple>
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

	@Override
	public List<Tuple> getResultList()
	{
		criteria.multiselect(multiselects);
		TypedQuery<Tuple> query = prepareQuery();
		return query.getResultList();
	}

}
