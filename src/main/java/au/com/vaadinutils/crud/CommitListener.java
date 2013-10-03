package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;

public interface CommitListener<E>
{

	public void committed();
	public void selectedRowChanged(EntityItem<E> item);
	
}
