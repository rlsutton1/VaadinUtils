package au.com.vaadinutils.fields;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.ui.ComboBox;

/**
 * A combobox which is dependent on a parent combo box.
 * 
 * Imagine two entities with a parent child relationship. 
 * You create a EntityComboBox to display the parent entities.
 * You then create a DependantComboBox which links to the parent ComboBox.
 * When a user selects an entity in the parent combobox that selection
 * is used to filter the set of child entities displayed in the DependantComboBox.
 * 
 * Usage:
 * JpaContainer<Publication> publicationContainer;
 * JpaContainer<Edition> editionContainer;
 * 
 * 	publicationField = new EntityComboBox<Publication>("Publication", pulicationContainer, "Publication");
 *
 *	editionField = new DependantComboBox<Publication, Edition>("Edition", publicationField, editionContainer,
 *				"publication", "name");

 *  
 * @author bsutton
 * 
 * @param <Parent>
 *            the Entity type of the items contained in the Parent container
 * @param <E>
 *            the Entity type of the items contained in the Child container
 */
public class DependantComboBox<Parent extends CrudEntity, E extends CrudEntity> extends EntityComboBox<E>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param label label to display adjacent to the ComboBox.
	 * @param parent the parent ComboBox that this ComboBox is dependent on.
	 * @param childContainer the container used to fill the child ComboBox.
	 * @param childForeignAttribute attribute of the child entity that links back to the parent entity.
	 */
	public DependantComboBox(String label, final ComboBox parent, final EntityContainer<E> childContainer,
			final SingularAttribute<E, Parent> childForeignAttribute, final SingularAttribute<E, ? extends Object> displayAttribute)
	{
		super(label, childContainer, displayAttribute);

		addParentHandler(parent, childContainer, childForeignAttribute);
	}
	
	public DependantComboBox(String label, final ComboBox parent, final EntityContainer<E> childContainer,
			final SingularAttribute<E, Parent> childForeignAttribute, final String displayAttribute)
	{
		super(label, childContainer, displayAttribute);

		addParentHandler(parent, childContainer, childForeignAttribute);
	}


	private void addParentHandler(final ComboBox parent, final EntityContainer<E> childContainer,
			final SingularAttribute<E, Parent> childForeignAttribute)
	{
		parent.addValueChangeListener(new ValueChangeListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{
				@SuppressWarnings("unchecked")
				Parent parentEntity = ((Parent) parent.getConvertedValue());

				childContainer.removeAllContainerFilters();
				childContainer.addContainerFilter(new Compare.Equal(childForeignAttribute.getName(), parentEntity));
				DependantComboBox.this.setContainerDataSource(childContainer);
				DependantComboBox.this.setValue(null);
			}
		});
	}
}
