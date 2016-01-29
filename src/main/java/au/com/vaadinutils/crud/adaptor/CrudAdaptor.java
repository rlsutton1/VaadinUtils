package au.com.vaadinutils.crud.adaptor;

import java.util.List;

import com.vaadin.addon.jpacontainer.JPAContainer;

import au.com.vaadinutils.crud.CrudAction;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.HeadingPropertySet;

/**
 * a common crudAdpator interface, implemented by both BaseCrudAdaptor and
 * ChildCrudAdaptor.
 * 
 * This common interface aids in allowing a single class to be a parent
 * crud or a child crud
 * 
 * @author rsutton
 *
 * @param
 * 			<P>
 * @param <E>
 */
public interface CrudAdaptor<P, E extends CrudEntity>
{

	void init(Class<E> class1, JPAContainer<E> container, HeadingPropertySet<E> headings);

	void setSplitPosition(int normalSplitSize);

	void disallowDelete(boolean b);

	E getCurrent();

	JPAContainer<E> getContainer();

	E preNew(E previousEntity) throws InstantiationException, IllegalAccessException;

	void disallowNew(boolean b);

	void setLocked(boolean locked);

	List<CrudAction<E>> getDefaultCrudActions();

}
