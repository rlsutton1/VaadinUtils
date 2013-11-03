package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;

public interface CrudAction<E extends CrudEntity>
{
	public String toString();
	
	/**
	 * The crudaction that has this value set to true
	 * will be selected as the default crud action.
	 */
	public boolean isDefault();
	
	void exec(BaseCrudView<E> crud, EntityItem<E> entity);
}
