package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;

public interface ParentCrud<T extends CrudEntity>
{

	EntityItem<T> getContainerItem(Long id);

	void fieldGroupIsDirty(boolean b);

	T getCurrent();

	boolean isDirty();

	void reloadDataFromDB();

	void save();

	void setSplitPosition(float pos);


}
