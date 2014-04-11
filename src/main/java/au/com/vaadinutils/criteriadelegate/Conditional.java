package au.com.vaadinutils.criteriadelegate;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import au.com.vaadinutils.crud.CrudEntity;

public interface Conditional<E extends CrudEntity,K>
{

	public Predicate getPredicate(CriterBuilder<E> builder, CriteriaBuilder criteriaBuilder,
			Map<Class<?>, Join<E, ?>> joins);
}
