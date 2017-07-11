package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import au.com.vaadinutils.dao.JpaBaseDao.Condition;

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
 * final JpaDslBuilderGroup<TblSalesCustCallItem> queryGroup = new JpaDslBuilderGroup<>(TblSalesCustCallItem.class);
 * queryGroup.setCommon(new JpaDslBuilderGroupCommon<TblSalesCustCallItem>()
 * {
 * 		&#64;Override
 * 		public void conditionsWillBeAdded(JpaDslBuilder<TblSalesCustCallItem> builder,
 * 				List<Condition<TblSalesCustCallItem>> conditions)
 * 		{
 * 			conditions.add(builder.eq(TblSalesCustCallItem_.contact, contact));
 * 		}
 * });
 *  
 * queryGroup.addItem(new JpaDslBuilderGroupItem<TblSalesCustCallItem>()
 * {
 * 		&#64;Override
 * 		public void conditionsWillBeAdded(JpaDslBuilder<TblSalesCustCallItem> builder,
 * 				List<Condition<TblSalesCustCallItem>> conditions)
 * 		{
 * 			conditions.add(builder.eq(TblSalesCustCallItem_.salesperson, salesperson));
 * 		}
 * });
 *  
 * final List<TblSalesCustCallItem> results = queryGroup.getResults();
 * </code>
 * </pre>
 *
 */
public class JpaDslBuilderGroup<E>
{

	private List<JpaDslBuilderGroupItem<E>> builders = new ArrayList<>();
	private JpaDslBuilderGroupCommon<E> common;
	private Class<E> entityClass;
	private List<E> results;
	private boolean distinct = false;
	private List<JpaDslOrder> orders = new ArrayList<>();

	public JpaDslBuilderGroup(final Class<E> entityClass)
	{
		this.entityClass = entityClass;
	}

	public void addItem(JpaDslBuilderGroupItem<E> builder)
	{
		builders.add(builder);
	}

	public interface JpaDslBuilderGroupItem<E>
	{
		public void conditionsWillBeAdded(final JpaDslBuilder<E> builder, final List<Condition<E>> conditions);
	}

	public void setCommon(JpaDslBuilderGroupCommon<E> common)
	{
		this.common = common;
	}

	public interface JpaDslBuilderGroupCommon<E>
	{
		public void conditionsWillBeAdded(final JpaDslBuilder<E> builder, final List<Condition<E>> conditions);
	}

	public List<E> getResults()
	{
		final Collection<E> results;
		if (distinct)
		{
			results = new HashSet<>();
		}
		else
		{
			results = new ArrayList<>();
		}

		if (builders.size() > 0)
		{
			for (JpaDslBuilderGroupItem<E> builder : builders)
			{
				results.addAll(makeQuery(builder));
			}
		}
		else
		{
			results.addAll(makeQuery(null));
		}

		if (distinct)
		{
			this.results = new ArrayList<>(results);
		}
		else
		{
			this.results = (List<E>) results;
		}

		return this.results;
	}

	private List<E> makeQuery(final JpaDslBuilderGroupItem<E> builder)
	{
		final JpaDslBuilder<E> q = new JpaDslBuilder<>(entityClass);
		final List<Condition<E>> conditions = new LinkedList<>();

		if (common != null)
		{
			common.conditionsWillBeAdded(q, conditions);
		}

		if (builder != null)
		{
			builder.conditionsWillBeAdded(q, conditions);
		}

		if (distinct)
		{
			q.distinct();
		}

		q.where(conditions);

		for (JpaDslOrder order : orders)
		{
			q.orderBy(order.getField(), order.getAscending());
		}

		return q.getResultList();
	}

	public void distinct()
	{
		distinct = true;
	}

	public void orderBy(final String field, final boolean ascending)
	{
		orders.add(new JpaDslOrder(field, ascending));
	}
}
