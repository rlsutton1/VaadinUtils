package au.com.vaadinutils.criteriadelegate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

public class Equals<ROOT extends CrudEntity,  FIELD_TYPE> extends
		SimplePredicate<CrudEntity,  FIELD_TYPE>
{

	

	public Equals(SingularAttribute<ROOT, FIELD_TYPE> field, FIELD_TYPE value)
	{
		super(field, value);
	}

	@Override
	Predicate createPredicate(CriteriaBuilder criteriaBuilder, Path<FIELD_TYPE> expr, FIELD_TYPE value)
	{
		return criteriaBuilder.equal(expr, value);
	}

}
