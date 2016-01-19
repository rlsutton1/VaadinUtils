package au.com.vaadinutils.dao;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
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
		return super.getResultList();
	}
	
	@Override
	public Tuple getSingleResult()
	{
		criteria.multiselect(multiselects);
		return super.getSingleResult();
	}

	@Override
	public Tuple getSingleResultOrNull()
	{
		criteria.multiselect(multiselects);
		return super.getSingleResultOrNull();
	}

	public <T extends Number> Expression<T> sum(SingularAttribute<E, T> attribute)
	{
		Expression<T> sum = builder.sum(root.get(attribute));
		multiselects.add(sum);
		
		return sum;
	}

}
