package au.com.vaadinutils.criteriadelegate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

public class Like<ROOT extends CrudEntity, V> extends SimplePredicate<CrudEntity, String>
{

	public Like(SingularAttribute<ROOT, String> field, String value)
	{
		super(field, value);
	}

	@Override
	Predicate createPredicate(CriteriaBuilder criteriaBuilder, Path<String> expr, String value)
	{
		return criteriaBuilder.like(expr, value);
	}

}
