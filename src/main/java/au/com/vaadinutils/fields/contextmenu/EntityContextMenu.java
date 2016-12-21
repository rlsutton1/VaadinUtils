package au.com.vaadinutils.fields.contextmenu;

import org.vaadin.peter.contextmenu.ContextMenu;

public class EntityContextMenu<E> extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	protected E targetEntity;

	public E getTargetEntity()
	{
		return targetEntity;
	}
}
