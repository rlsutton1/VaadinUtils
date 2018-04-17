package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;

public interface ParentCrud<T extends CrudEntity>
{

	EntityItem<T> getContainerItem(Long id);

	void fieldGroupIsDirty(boolean b);

	T getCurrent();

	boolean isDirty();

	void reloadDataFromDB();

	void save();

	void setSplitPosition(float pos);

	void setSearchFilterText(String string);

	void setMainView(boolean isMainView);

	void addChildCrudListener(ChildCrudListener<T> listener);

	void removeChildCrudListener(ChildCrudListener<T> listener);

	ValidatingFieldGroup<T> getFieldGroup();

	EntityItem<T> getNewEntity();

	BaseCrudSaveCancelButtonTray getButtonLayout();

	CrudSecurityManager getSecurityManager();

	boolean isNew();

	JPAContainer<T> getContainer();

	void addAdvancedModeListener(AdvancedModeListener listener);

	boolean isAdvancedMode();

}
