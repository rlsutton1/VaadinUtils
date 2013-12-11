package au.com.vaadinutils.crud;

import java.util.Collection;

public interface ChildCrudEventHandler<E extends CrudEntity>
{

	public void entitiesDeleted(Collection<E> entities);
}
