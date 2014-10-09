package au.com.vaadinutils.crud;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import au.com.vaadinutils.fields.TableCheckBoxSelect;

import com.google.common.base.Preconditions;
import com.vaadin.data.Container;

public class SelectableEntityTable<E> extends TableCheckBoxSelect
{
	// private static transient Logger logger =
	// LogManager.getLogger(SelectableEntityTable.class);

	private static final long serialVersionUID = 1L;

	public SelectableEntityTable(Container.Filterable childContainer, HeadingPropertySet<E> headingPropertySet)
	{
		super();
		setContainerDataSource(childContainer);
		buildSelectableContainer(headingPropertySet);

	}

	/**
	 * copy the containers items into a new IndexContainer which also contains
	 * the 'selectable' property.
	 * 
	 * @param entityContainer2
	 * @param headingPropertySet2
	 * @return
	 */
	private void buildSelectableContainer(HeadingPropertySet<E> visibleColumns)
	{
		List<String> colsToShow = new LinkedList<String>();
		for (HeadingToPropertyId<E> column : visibleColumns.getColumns())
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

		for (HeadingToPropertyId<E> column : visibleColumns.getColumns())
		{
			this.setColumnHeader(column.getPropertyId(), column.getHeader());
			if (column.getWidth() != null)
			{
				setColumnWidth(column.getPropertyId(), column.getWidth());
			}

		}
		
		setColumnHeader(TableCheckBoxSelect.TABLE_CHECK_BOX_SELECT, "");

	}

	/**
	 * returns an array of selected entities.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<Long> getSelectedIds()
	{
		return (Collection<Long>) super.getSelectedItems();
	}

	public void applyFilter(final Filter filter)
	{
		/* Reset the filter for the Entity Container. */
		resetFilters();
		((Container.Filterable) getContainerDataSource()).addContainerFilter(filter);

	}

	protected void resetFilters()
	{
		((Container.Filterable) getContainerDataSource()).removeAllContainerFilters();
	}

}
