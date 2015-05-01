package au.com.vaadinutils.dao;

import javax.persistence.criteria.Predicate;

public interface Condition<T>
{

    Predicate getPredicates();

}
