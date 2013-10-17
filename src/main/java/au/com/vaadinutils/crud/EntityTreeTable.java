package au.com.vaadinutils.crud;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Property;
import com.vaadin.ui.Tree;

public class EntityTreeTable<E> extends Tree implements EntityList<E>
{

	private static final long serialVersionUID = 1L;
	private JPAContainer<E> entityContainer;
	private RowChangeListener<E> rowChangeListener;
	private HeadingPropertySet<E> visibleColumns;

	Logger logger = Logger.getLogger(EntityTreeTable.class);

	public EntityTreeTable(JPAContainer<E> entityContainer, HeadingPropertySet<E> headingPropertySet)
	{
		this.entityContainer = entityContainer;
		this.visibleColumns = headingPropertySet;
	}

	public void setRowChangeListener(RowChangeListener<E> rowChangeListener)
	{
		this.rowChangeListener = rowChangeListener;
	}

	public void init()
	{

		this.setContainerDataSource(entityContainer);

		List<String> colsToShow = new LinkedList<String>();
		for (HeadingToPropertyId<E> column : visibleColumns.getColumns())
		{
			colsToShow.add(column.getPropertyId());

			if (column.isGenerated())
			{
	//			addGeneratedColumn(column.getPropertyId(), column.getColumnGenerator());
			}
			else
			{

				Preconditions.checkArgument(this.getContainerPropertyIds().contains(column.getPropertyId()),
						column.getPropertyId() + " is not a valid property id, valid property ids are "
								+ this.getContainerPropertyIds().toString());
			}
		}
	//	this.setVisibleColumns(colsToShow.toArray());

//		for (HeadingToPropertyId<E> column : visibleColumns.getColumns())
//		{
//			this.setColumnHeader(column.getPropertyId(), column.getHeader());
//		}

		this.setSelectable(true);
		this.setImmediate(true);

		this.addValueChangeListener(new Property.ValueChangeListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{
				if (EntityTreeTable.this.rowChangeListener != null)
				{
					 Object entityId =  EntityTreeTable.this.getValue();

					if (entityId != null) // it can be null when a row is being
											// deleted.
					{
						EntityItem<E> entity = EntityTreeTable.this.entityContainer.getItem(entityId); // .getEntity();
						EntityTreeTable.this.rowChangeListener.rowChanged(entity);
					}
					else
						EntityTreeTable.this.rowChangeListener.rowChanged(null);
				}
			}
		});
	}

	public void superChangeVariables(final Object source, final Map<String, Object> variables)
	{

	}

	/**
	 * Hooking this allows us to veto the user selecting a new row. if there is
	 * a rowChangeListener we will prevent the row change.
	 * 
	 * it's up to the listener to callback on superChangeVariables to perform
	 * the row change if row change should be allowed.
	 */
	@Override
	public void changeVariables(final Object source, final Map<String, Object> variables)
	{
		if (variables.containsKey("selected"))
		{

			if (EntityTreeTable.this.rowChangeListener != null)
			{
				EntityTreeTable.this.rowChangeListener.allowRowChange(new RowChangeCallback()
				{
					@Override
					public void allowRowChange()
					{
						EntityTreeTable.super.changeVariables(source, variables);
					}
				});
				markAsDirty();
			}
			else
			{
				EntityTreeTable.super.changeVariables(source, variables);
			}
		}
		else
			super.changeVariables(source, variables);
	}

	public E getCurrent()
	{
		Long entityId = (Long) this.getValue();
		E entity = this.entityContainer.getItem(entityId).getEntity();

		return entity;

	}

	@Override
	public void setSortEnabled(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object firstItemId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getCurrentPageFirstItemId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object prevItemId(Object entityId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This nasty piece of work exists to stop the following exception being
	 * 
	 * thrown. java.lang.IllegalArgumentException: wrong number of arguments
	 * sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	 * sun.reflect.NativeMethodAccessorImpl
	 * .invoke(NativeMethodAccessorImpl.java:57)
	 * sun.reflect.DelegatingMethodAccessorImpl
	 * .invoke(DelegatingMethodAccessorImpl.java:43)
	 * java.lang.reflect.Method.invoke(Method.java:606)
	 * com.vaadin.addon.jpacontainer
	 * .metadata.ClassMetadata.getPropertyValue(ClassMetadata.java:168)
	 * com.vaadin.addon.jpacontainer.metadata.ClassMetadata.getPropertyValue(
	 * ClassMetadata.java:343)
	 * com.vaadin.addon.jpacontainer.PropertyList.getPropertyValue
	 * (PropertyList.java:677)
	 * com.vaadin.addon.jpacontainer.JPAContainerItem$ItemProperty
	 * .getRealValue(JPAContainerItem.java:176)
	 * com.vaadin.addon.jpacontainer.JPAContainerItem$ItemProperty
	 * .getValue(JPAContainerItem.java:163)
	 * com.vaadin.ui.Table.formatPropertyValue(Table.java:4012)
	 * com.vaadin.ui.Table.getPropertyValue(Table.java:3956)
	 * com.vaadin.ui.Table.parseItemIdToCells(Table.java:2308)
	 * com.vaadin.ui.Table.getVisibleCellsNoCache(Table.java:2147)
	 * com.vaadin.ui.Table.refreshRenderedCells(Table.java:1668)
	 * com.vaadin.ui.Table.enableContentRefreshing(Table.java:3143)
	 * com.vaadin.ui.Table.setContainerDataSource(Table.java:2712)
	 * com.vaadin.ui.Table.setContainerDataSource(Table.java:2653)
	 * au.org.scoutmaster.views.ContactTable.init(ContactTable.java:46)
	 * 
	 */
//	@Override
//	protected String formatPropertyValue(Object rowId, Object colId, Property<?> property)
//	{
//		if (property.getType() == Set.class)
//		{
//			return null;
//		}
//		try
//		{
//			property.getValue();
//
//		}
//		catch (Exception e)
//		{
//			return null;
//		}
//		String ret = null;
//		try
//		{
//			ret = super.formatPropertyValue(rowId, colId, property);
//		}
//		catch (Exception e)
//		{
//			logger.error("value: " + property.getValue() + " type: " + property.getType(), e);
//			ret = e.getMessage();
//		}
//		return ret;
//	}

	

}
