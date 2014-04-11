package au.com.vaadinutils.criteriadelegate;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import au.com.vaadinutils.crud.CrudEntity;

public class Or <E extends CrudEntity,K> implements Conditional<E,K>
{

	private Conditional<E,K> cond1;
	private Conditional<E,K> cond2;

	public Or(Conditional<E,K> cond1, Conditional<E,K> cond2)
	{
		this.cond1 = cond1;
		this.cond2 = cond2;
	}

	@Override
	public Predicate getPredicate(CriterBuilder<E> builder, CriteriaBuilder criteriaBuilder,
			Map<Class<?>, Join<E, ?>> joins)	{
		return criteriaBuilder.or(cond1.getPredicate(builder,criteriaBuilder, joins),
				cond2.getPredicate(builder,criteriaBuilder, joins));
	}

}
