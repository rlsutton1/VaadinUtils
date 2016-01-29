package au.com.vaadinutils.crud.adaptor;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.addon.jpacontainer.EntityItem;

import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.crud.CrudEntity;

/**
 * 
 * @author rsutton
 *
 * @param <P>
 * @param <E>
 */
public interface ChildCrudInterface<P extends CrudEntity,E extends ChildCrudEntity> extends CrudInterface<E>
{

	
	SingularAttribute<E, String>  getGuidAttribute();

	void associateChild(P newParent, E child);

	String getNewButtonActionLabel();

	void selectedParentRowChanged(EntityItem<P> item);

}
