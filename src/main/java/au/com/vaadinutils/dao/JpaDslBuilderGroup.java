package au.com.vaadinutils.dao;

import java.util.LinkedList;
import java.util.List;

/**
 * Sometimes it is faster to run multiple queries returning the same entity than
 * it is to run a single (potentially slow) query with multiple joins and
 * clauses. This class can be used to group the queries and return the results
 * from them in a single set.
 * 
 * Usage example:
 * 
 * <pre>
 * <code>
 * JpaDslBuilderGroup<TblContact> queryGroup = new JpaDslBuilderGroup<>(TblContact.class);
 * queryGroup.distinct();
 * 
 * queryGroup.addItem(new JpaDslBuilderItem<TblContact>()
 * {
 * 	@Override
 * 	public void conditionsWillBeAdded(JpaDslBuilder<TblContact> builder)
 * 	{
 * 		final Condition<TblContact> primaryCondition = builder.in(builder.join(TblContact_.tblCustomers),
 * 					TblCustomer_.customerID, parentIds);
 * 			final Condition<TblContact> mainCriteria = applySearchFilter(searchFilterEnabled, contactShareEnabled,
 * 					builder, account, primaryCondition);
 * 			builder.where(mainCriteria);
 * 		}
 * 	});
 * }
 * 
 * final List<TblContact> results = queryGroup.getResults();
 * </code>
 * </pre>
 *
 */
public class JpaDslBuilderGroup<E>
{

	List<JpaDslBuilderItem<E>> builders = new LinkedList<>();
	private Class<E> entityClass;
	private List<E> results = new LinkedList<>();
	private boolean distinct = false;

	public JpaDslBuilderGroup(final Class<E> entityClass)
	{
		this.entityClass = entityClass;
	}

	public void addItem(JpaDslBuilderItem<E> builder)
	{
		builders.add(builder);
	}

	public interface JpaDslBuilderItem<E>
	{
		public void conditionsWillBeAdded(final JpaDslBuilder<E> builder);
	}

	public List<E> getResults()
	{
		for (JpaDslBuilderItem<E> builder : builders)
		{
			final JpaDslBuilder<E> q = new JpaDslBuilder<E>(entityClass);
			builder.conditionsWillBeAdded(q);
			if (distinct)
				q.distinct();
			results.addAll(q.getResultList());
		}

		return results;
	}

	public void distinct()
	{
		distinct = true;
	}
}
