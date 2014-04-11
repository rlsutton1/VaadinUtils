package au.com.vaadinutils.criteriadelegate;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

public abstract class SimplePredicate<ROOT extends CrudEntity, FIELD_TYPE> implements Conditional<ROOT, FIELD_TYPE>
{
	private FIELD_TYPE value;
	private SingularAttribute<?, FIELD_TYPE> field;

	public SimplePredicate(SingularAttribute<?, FIELD_TYPE> field, FIELD_TYPE value)
	{

		this.field = field;
		this.value = value;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Predicate getPredicate(CriterBuilder<ROOT> builder, CriteriaBuilder criteriaBuilder,
			Map<Class<?>, Join<ROOT, ?>> joins)
	{
		Path<FIELD_TYPE> expr = null;
		Join<ROOT, ?> join = joins.get(field.getDeclaringType().getJavaType());
		if (join == null)
		{
			expr = builder.entityRoot.get((SingularAttribute) field);
		}
		else
		{
			expr = join.get((SingularAttribute) field);
		}
		return createPredicate(criteriaBuilder, expr, value);
	}

	abstract Predicate createPredicate(CriteriaBuilder criteriaBuilder, Path<FIELD_TYPE> expr, FIELD_TYPE value);

}
