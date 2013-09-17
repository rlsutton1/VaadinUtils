package au.com.vaadinutils.fields;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectConverter;
import com.vaadin.ui.ComboBox;

/**
 * 
 * Provides a combobox bound to an Entity Container.
 * 
 * @author bsutton
 * 
 * @param <Parent>
 *            the Entity type of the items contained in the Parent container
 * @param <Child>
 *            the Entity type of the items contained in the Child container
 */
public class EntityComboBox<E extends CrudEntity> extends ComboBox
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param label
	 *            label to display adjacent to the ComboBox.
	 * @param childContainer
	 *            the entity container used to fill the child ComboBox.
	 * @param displayAttribute
	 *            the attribute of the child entity that is used to display in the combo box.
	 */
	public EntityComboBox(String label, final EntityContainer<E> childContainer,
			final SingularAttribute<E, ? extends Object> displayAttribute)
	{
		super(label);
		init(childContainer, displayAttribute.getName());
	}
	
	public EntityComboBox(String label, final EntityContainer<E> childContainer,
			final String displayAttribute)
	{
		super(label);
		init(childContainer, displayAttribute);
	}


	private void init(final EntityContainer<E> childContainer,
			final String displayAttribute)
	{
		this.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		this.setItemCaptionPropertyId(displayAttribute);
		this.setContainerDataSource(childContainer);
		SingleSelectConverter<E> childConverter = new SingleSelectConverter<E>(this);
		this.setConverter(childConverter);
		this.setNewItemsAllowed(false);
		this.setNullSelectionAllowed(false);
		this.setTextInputAllowed(false);
		this.setImmediate(true);
	}
}
