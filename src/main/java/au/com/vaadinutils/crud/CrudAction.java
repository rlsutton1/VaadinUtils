package au.com.vaadinutils.crud;

import java.io.Serializable;

import com.vaadin.addon.jpacontainer.EntityItem;

public interface CrudAction<E extends CrudEntity> extends Serializable
{
	public String toString();
	
	/**
	 * The crudaction that has this value set to true
	 * will be selected as the default crud action.
	 */
	public boolean isDefault();
	
	public boolean showPreparingDialog();
	
	void exec(BaseCrudView<E> crud, EntityItem<E> entity);
}
