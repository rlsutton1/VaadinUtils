package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Or;

/**
 * Vaadin does not have support for IN filter, so this class can be used to
 * simulate such a filter
 */
public class InFilter
{
	public static Or getLongFilter(final SingularAttribute<?, Long> attribute, final List<Long> longValues)
	{
		final List<Filter> orFilters = new ArrayList<Filter>(longValues.size());
		for (Long longValue : longValues)
		{
				orFilters.add(new Equal(attribute.getName(), longValue));
		}

		return new Or(orFilters.toArray(new Filter[orFilters.size()]));
	}
}
