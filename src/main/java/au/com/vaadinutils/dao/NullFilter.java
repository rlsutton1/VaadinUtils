package au.com.vaadinutils.dao;

import java.util.Vector;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;

/**
 * Allows one or more filters to be passed in and the null filters are removed
 * while the valid ones are added to the query
 */
public class NullFilter
{

	public static And and(Filter... filters)
	{
		final Vector<Filter> validFilters = new Vector<Filter>();
		for (Filter filter : filters)
		{
			if (filter != null)
				validFilters.add(filter);
		}

		return new And(validFilters.toArray(new Filter[validFilters.size()]));
	}
}
