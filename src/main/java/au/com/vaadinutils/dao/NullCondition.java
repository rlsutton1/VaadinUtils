package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.List;

import au.com.vaadinutils.dao.JpaBaseDao.Condition;

/**
 * Allows one or more conditions to be passed in and the null filters are
 * removed while the valid ones are added to the query
 */
public class NullCondition
{
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <E> Condition<E> and(final JpaDslAbstract<E, ?> queryBuilder, final Condition<E>... conditions)
	{
		final List<Condition<E>> validConditions = new ArrayList<>();
		for (Condition<E> condition : conditions)
		{
			if (condition != null)
				validConditions.add(condition);
		}

		if (validConditions.size() == 1)
			return validConditions.get(0);

		return queryBuilder.and(validConditions.toArray(new Condition[validConditions.size()]));
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <E> Condition<E> or(final JpaDslAbstract<E, ?> queryBuilder, Condition<E>... conditions)
	{
		final List<Condition<E>> validConditions = new ArrayList<>();
		for (Condition<E> condition : conditions)
		{
			if (condition != null)
				validConditions.add(condition);
		}

		if (validConditions.size() == 1)
			return validConditions.get(0);

		return queryBuilder.or(validConditions.toArray(new Condition[validConditions.size()]));
	}
}
