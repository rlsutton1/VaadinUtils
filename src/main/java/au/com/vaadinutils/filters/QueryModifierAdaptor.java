package au.com.vaadinutils.filters;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;

import com.vaadin.addon.jpacontainer.QueryModifierDelegate;

/**
 * An adaptor for QueryModifierDelegate so you just have to implement the methods of interest.
 * @author bsutton
 *
 */
public class QueryModifierAdaptor implements QueryModifierDelegate
{
	private static final long serialVersionUID = 1L;

	@Override
	public void queryWillBeBuilt(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query)
	{
	}

	@Override
	public void queryHasBeenBuilt(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query)
	{
	}

	@Override
	public void filtersWillBeAdded(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, List<Predicate> predicates)
	{
	}

	@Override
	public void filtersWereAdded(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query)
	{
	}

	@Override
	public void orderByWillBeAdded(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, List<Order> orderBy)
	{
	}

	@Override
	public void orderByWasAdded(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query)
	{
	}

}
