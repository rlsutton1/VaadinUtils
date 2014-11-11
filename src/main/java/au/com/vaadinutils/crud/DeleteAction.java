package au.com.vaadinutils.crud;

import java.io.IOException;

import com.vaadin.addon.jpacontainer.EntityItem;

public interface DeleteAction<E>
{
	public void delete(final EntityItem<E> entity) throws Exception;
}
