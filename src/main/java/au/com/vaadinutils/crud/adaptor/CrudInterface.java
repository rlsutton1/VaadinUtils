package au.com.vaadinutils.crud.adaptor;

import java.util.List;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Component;

import au.com.vaadinutils.crud.CrudAction;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.ValidatingFieldGroup;

/**
 * classes wishing to be detached via an interface from the crud implementation
 * need to implement this interface
 * 
 * @author rsutton
 *
 * @param <E>
 */
public interface CrudInterface<E extends CrudEntity>
{

	Component buildEditor(ValidatingFieldGroup<E> fieldGroup2);

	Filter getContainerFilter(String filterString, boolean advancedSearchActive);

	String getTitleText();

	List<CrudAction<E>> getCrudActions();

	/**
	 * if you don't need to modify it, just return newEntity
	 * 
	 * @param newEntity
	 * @param previousEntity
	 * @return
	 */
	E preNew(E newEntity, E previousEntity);

	void rowChanged(EntityItem<E> item);

	void postNew(EntityItem<E> newEntity);

	void interceptSaveValues(EntityItem<E> entityItem) throws Exception;

	void resetFilters(JPAContainer<E> container);

	EntityItem<E> createNewEntity(EntityItem<E> newEntity, E previousEntity) throws InstantiationException, IllegalAccessException;

}
