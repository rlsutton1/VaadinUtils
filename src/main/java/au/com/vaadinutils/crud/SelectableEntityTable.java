package au.com.vaadinutils.crud;

import java.util.Collection;

import au.com.vaadinutils.fields.TableCheckBoxSelect;

import com.vaadin.data.Container;

public class SelectableEntityTable<E> extends TableCheckBoxSelect
{
	// private static transient Logger logger =
	// LogManager.getLogger(SelectableEntityTable.class);

	private static final long serialVersionUID = 1L;
	private String uniqueId;

	public SelectableEntityTable(Container.Filterable childContainer, HeadingPropertySet<E> headingPropertySet,
			String uniqueId)
	{
		super();
		this.uniqueId = uniqueId;
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
		visibleColumns.applyToTable(this, uniqueId);

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
