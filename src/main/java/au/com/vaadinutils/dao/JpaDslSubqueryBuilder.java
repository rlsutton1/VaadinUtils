package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

public class JpaDslSubqueryBuilder<E, K>
{

    public JpaDslSubqueryBuilder(EntityManager entityManager, Class<K> target, CriteriaQuery<E> criteria, Root<E> root)
    {
	// TODO Auto-generated constructor stub
    }

    public Subquery<?> getSubQuery()
    {
	// TODO Auto-generated method stub
	return null;
    }

}
