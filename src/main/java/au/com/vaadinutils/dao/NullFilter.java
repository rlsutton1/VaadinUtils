package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Or;

/**
 * Allows one or more filters to be passed in and the null filters are removed
 * while the valid ones are added to the query
 */
public class NullFilter
{
	public static Filter and(final Filter... filters)
	{
		final List<Filter> validFilters = new ArrayList<Filter>();
		for (Filter filter : filters)
		{
			if (filter != null)
				validFilters.add(filter);
		}
		
		if (validFilters.size() == 1)
			return validFilters.get(0);

		return new And(validFilters.toArray(new Filter[validFilters.size()]));
	}

	public static Filter or(final Filter... filters)
	{
		final List<Filter> validFilters = new ArrayList<Filter>();
		for (Filter filter : filters)
		{
			if (filter != null)
				validFilters.add(filter);
		}
		
		if (validFilters.size() == 1)
			return validFilters.get(0);

		return new Or(validFilters.toArray(new Filter[validFilters.size()]));
	}
}
