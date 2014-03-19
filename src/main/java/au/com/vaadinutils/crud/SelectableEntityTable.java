package au.com.vaadinutils.crud;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

public class SelectableEntityTable<E> extends Table
{
	private static  transient Logger logger   =  LogManager.getLogger(SelectableEntityTable.class);

	private static final String HEADING_SELECTED = "Selected";
	private static final String SELECTABLE_ENTITY_TABLE_SELECTED = "SelectableEntityTableProperty";
	private static final long serialVersionUID = 1L;
	private IndexedContainer selectableContainer;
	private RowChangeListener<E> rowChangeListener;

	private HeadingPropertySet<E> headingPropertySet;

	private ArrayList<Long> selectedIds = null;

	public SelectableEntityTable(EntityContainer<E> childContainer, HeadingPropertySet<E> headingPropertySet)
	{
		this.headingPropertySet = headingPropertySet;

		// Add the 'selected' column in
		this.addContainerProperty(SELECTABLE_ENTITY_TABLE_SELECTED, Boolean.class, new Boolean(true), HEADING_SELECTED,
				null, Align.CENTER);
		headingPropertySet.getColumns().add(
				new HeadingToPropertyId<E>(HEADING_SELECTED, SELECTABLE_ENTITY_TABLE_SELECTED,
						new SelectedCheckBoxGenerator()));

		this.selectableContainer = buildSelectableContainer(childContainer, headingPropertySet);

		init();
	}

	/**
	 * copy the containers items into a new IndexContainer which also contains
	 * the 'selectable' property.
	 * 
	 * @param entityContainer2
	 * @param headingPropertySet2
	 * @return
	 */
	private IndexedContainer buildSelectableContainer(EntityContainer<E> entityContainer2,
			HeadingPropertySet<E> headingPropertySet2)
	{
		IndexedContainer selectable = new IndexedContainer();

		for (HeadingToPropertyId<E> heading : headingPropertySet2.getColumns())
		{
			if (heading.getPropertyId().equals(SELECTABLE_ENTITY_TABLE_SELECTED))
				selectable.addContainerProperty(heading.getPropertyId(), Boolean.class, new Boolean(true));
			else
				selectable.addContainerProperty(heading.getPropertyId(),
						entityContainer2.getType(heading.getPropertyId()), null);
		}

		for (Object itemId : entityContainer2.getItemIds())
		{
			Item existingItem = entityContainer2.getItem(itemId);

			Item newItem = selectable.addItem(itemId);
			for (HeadingToPropertyId<E> heading : headingPropertySet2.getColumns())
			{
				@SuppressWarnings("unchecked")
				Property<Object> property = newItem.getItemProperty(heading.getPropertyId());
				if (heading.getPropertyId().equals(SELECTABLE_ENTITY_TABLE_SELECTED))
					property.setValue(new Boolean(true));
				else
					property.setValue(existingItem.getItemProperty(heading.getPropertyId()).getValue());
			}
		}
		return selectable;
	}

	public void setRowChangeListener(RowChangeListener<E> rowChangeListener)
	{
		this.rowChangeListener = rowChangeListener;
	}

	public void init()
	{

		this.setContainerDataSource(selectableContainer);

		List<String> colsToShow = new LinkedList<String>();
		for (HeadingToPropertyId<E> column : this.headingPropertySet.getColumns())
		{
			colsToShow.add(column.getPropertyId());

			if (column.isGenerated())
			{
				addGeneratedColumn(column.getPropertyId(), column.getColumnGenerator());
			}
			else
			{
				Preconditions.checkArgument(this.getContainerPropertyIds().contains(column.getPropertyId()),
						column.getPropertyId() + " is not a valid property id, valid property ids are "
								+ this.getContainerPropertyIds().toString());
			}
		}
		this.setVisibleColumns(colsToShow.toArray());

		for (HeadingToPropertyId<E> column : headingPropertySet.getColumns())
		{
			this.setColumnHeader(column.getPropertyId(), column.getHeader());
		}

		this.setSelectable(true);
		this.setImmediate(true);

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

			if (SelectableEntityTable.this.rowChangeListener != null)
			{
				SelectableEntityTable.this.rowChangeListener.allowRowChange(new RowChangeCallback()
				{
					@Override
					public void allowRowChange()
					{
						SelectableEntityTable.super.changeVariables(source, variables);
					}
				});
				markAsDirty();
			}
			else
			{
				SelectableEntityTable.super.changeVariables(source, variables);
			}
		}
		else
			super.changeVariables(source, variables);
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
	@Override
	protected String formatPropertyValue(Object rowId, Object colId, Property<?> property)
	{
		if (property.getType() == Set.class)
		{
			return null;
		}
		try
		{
			property.getValue();

		}
		catch (Exception e)
		{
			return null;
		}
		String ret = null;
		try
		{
			ret = super.formatPropertyValue(rowId, colId, property);
		}
		catch (Exception e)
		{
			logger.error("value: " + property.getValue() + " type: " + property.getType(), e);
			ret = e.getMessage();
		}
		return ret;
	}

	public class SelectedCheckBoxGenerator implements ColumnGenerator
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object generateCell(final Table source, final Object itemId, Object columnId)
		{
			final CheckBox button = new CheckBox();

			final Item item = source.getItem(itemId);
			@SuppressWarnings("unchecked")
			Property<Boolean> itemProperty = item.getItemProperty(SELECTABLE_ENTITY_TABLE_SELECTED);
			Boolean selected = new Boolean(true);
			if (itemProperty != null)
				selected = itemProperty.getValue();
			button.setValue(selected);

			button.addValueChangeListener(new ValueChangeListener()
			{
				private static final long serialVersionUID = 1L;

				@SuppressWarnings("unchecked")
				@Override
				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
				{
					Boolean selected = (Boolean) event.getProperty().getValue();
					Item item = source.getItem(itemId);
					item.getItemProperty(SELECTABLE_ENTITY_TABLE_SELECTED).setValue(selected);
					// reset the cache.
					selectedIds = null;
				}

			});

			return button;
		}

	}

	/**
	 * returns an array of selected entities.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Long> getSelectedIds()
	{
<<<<<<< HEAD
			selectedIds = new ArrayList<Long>();
=======
		selectedIds = new ArrayList<Long>();
>>>>>>> branch 'master' of https://github.com/rlsutton1/VaadinUtils.git

<<<<<<< HEAD
			for (Object itemId : this.selectableContainer.getItemIds())
			{
				Item item = this.selectableContainer.getItem(itemId);
				Property<Boolean> property = item.getItemProperty(SELECTABLE_ENTITY_TABLE_SELECTED);
				if (property.getValue() == true)
					selectedIds.add((Long) itemId);
			}
=======
		for (Object itemId : this.selectableContainer.getItemIds())
		{
			Item item = this.selectableContainer.getItem(itemId);
			Property<Boolean> property = item.getItemProperty(SELECTABLE_ENTITY_TABLE_SELECTED);
			if (property.getValue() == true)
				selectedIds.add((Long) itemId);
		}
>>>>>>> branch 'master' of https://github.com/rlsutton1/VaadinUtils.git
		return selectedIds;
	}

	public void applyFilter(final Filter filter)
	{
		/* Reset the filter for the Entity Container. */
		resetFilters();
		selectableContainer.addContainerFilter(filter);
		// selectableContainer.discard();
<<<<<<< HEAD
		}
=======
	}
>>>>>>> branch 'master' of https://github.com/rlsutton1/VaadinUtils.git

	/**
	 * for child cruds, they overload this to ensure that the minimum necessary
	 * filters are always applied.
	 */
	protected void resetFilters()
	{
		selectableContainer.removeAllContainerFilters();
	}

}
