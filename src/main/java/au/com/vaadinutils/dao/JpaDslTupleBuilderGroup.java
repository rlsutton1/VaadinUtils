package au.com.vaadinutils.dao;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Tuple;
import javax.persistence.metamodel.SingularAttribute;

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
 * JpaDslTupleBuilderGroup<TblContact> queryGroup = new JpaDslTupleBuilderGroup<>(TblContact.class);
 * queryGroup.distinct();
 * 
 * queryGroup.addItem(new JpaDslTupleBuilderItem<TblContact>()
 * {
 * 	@Override
 * 	public void conditionsWillBeAdded(JpaDslTupleBuilder<TblContact> builder)
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
 * queryGroup.multiselect(TblContact_.contactID);
 * 
 * final List<Long> contactIds = new LinkedList<>();
 * for (Tuple result : queryGroup.getResults())
 * {
 *		contactIds.add(queryGroup.get(result, TblContact_.contactID));
 * }
 * </code>
 * </pre>
 *
 */
public class JpaDslTupleBuilderGroup<E>
{

	List<JpaDslTupleBuilderItem<E>> builders = new LinkedList<>();
	private Class<E> entityClass;
	private List<Tuple> results = new LinkedList<>();
	private boolean distinct = false;
	private Map<SingularAttribute<E, ?>, Integer> multiselects = new LinkedHashMap<>();
	private int positionCounter = 0;

	public JpaDslTupleBuilderGroup(final Class<E> entityClass)
	{
		this.entityClass = entityClass;
	}

	public void addItem(JpaDslTupleBuilderItem<E> builder)
	{
		builders.add(builder);
	}

	public interface JpaDslTupleBuilderItem<E>
	{
		public void conditionsWillBeAdded(final JpaDslTupleBuilder<E> builder);
	}

	public <T> void multiselect(SingularAttribute<E, T> attribute)
	{
		multiselects.put(attribute, positionCounter++);
	}

	public <T> T get(final Tuple tuple, final SingularAttribute<E, T> attribute)
	{
		final Integer tuplePosition = multiselects.get(attribute);
		return tuple.get(tuplePosition, attribute.getBindableJavaType());
	}

	public List<Tuple> getResults()
	{
		for (JpaDslTupleBuilderItem<E> builder : builders)
		{
			final JpaDslTupleBuilder<E> q = new JpaDslTupleBuilder<E>(entityClass);

			builder.conditionsWillBeAdded(q);

			if (distinct)
				q.distinct();
			for (Entry<SingularAttribute<E, ?>, Integer> multiselect : multiselects.entrySet())
			{
				q.multiselect(multiselect.getKey());
			}

			results.addAll(q.getResultList());
		}

		return results;
	}

	public void distinct()
	{
		distinct = true;
	}
}
