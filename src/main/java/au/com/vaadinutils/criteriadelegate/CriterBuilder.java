package au.com.vaadinutils.criteriadelegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

/**
 * this class is intended to be used in a QueryModifierDelegate, as a builder to
 * build a query using the provided query stub
 * <p>
 * example usage...
 * <p>
 * CriterBuilder<Tblcallrecord> builder = new
 * CriterBuilder<Tblcallrecord>(criteriaBuilder, (CriteriaQuery<Tblcallrecord>)
 * query, predicates);
 * <p>
 * 
 * builder.join(Tblcallrecord_.account, JoinType.LEFT)<br>
 * .where(<br>
 * builder.and(<br>
 * builder.and(<br>
 * builder.equals( Tblcallrecord_.vSource, "400"), <br>
 * builder.equals(Tblcallrecord_.account, Account_.login, "100")),<br>
 * builder.like(Tblcallrecord_.uniqueCallId, "%497%")))<br>
 * .build();
 * 
 * 
 * @author rsutton
 * 
 * @param <E>
 */
public class CriterBuilder<E extends CrudEntity>
{

	Root<E> entityRoot = null;
	private CriteriaBuilder criteriaBuilder;
	private CriteriaQuery<E> query;
	// private List<Predicate> predicates;
	private Map<Class<?>, Join<E, ?>> joins = new HashMap<Class<?>, Join<E, ?>>();
	private Conditional<E, ?> conditional;

	@SuppressWarnings("unchecked")
	public CriterBuilder(CriteriaBuilder criteriaBuilder, CriteriaQuery<E> query, List<Predicate> predicates)
	{
		this.criteriaBuilder = criteriaBuilder;
		this.query = query;
		// this.predicates = predicates;
		entityRoot = (Root<E>) query.getRoots().iterator().next();
	}

	public CriterBuilder<E> join(SingularAttribute<E, ?> joinColumn, JoinType joinType)
	{

		Join<E, ?> join = entityRoot.join(joinColumn, joinType);

		joins.put(joinColumn.getBindableJavaType(), join);
		return this;

	}

	public <K> CriterBuilder<E> where(Conditional<E, K> cond)
	{
		this.conditional = cond;
		return this;
	}

	public <K> Conditional<E, K> and(Conditional<E, K> arg1, Conditional<E, K> arg2)
	{
		return new And<E, K>(arg1, arg2);
	}

	public <K> Conditional<E, K> or(Conditional<E, K> arg1, Conditional<E, K> arg2)
	{
		return new Or<E, K>(arg1, arg2);
	}

	@SuppressWarnings("unchecked")
	public <K> Conditional<E, K> equals(SingularAttribute<E, K> arg1, K arg2)
	{
		return (Conditional<E, K>) new Equals<E, K>(arg1, arg2);
	}

	@SuppressWarnings("unchecked")
	public <JOIN extends CrudEntity> Conditional<E, String> like(SingularAttribute<E, String> arg1, String arg2)
	{
		return (Conditional<E, String>) new Like<E, JOIN>(arg1, arg2);
	}

	public void build()
	{
		if (conditional != null)
		{
			query.where(conditional.getPredicate(this, criteriaBuilder, joins));
		}
	}

}
