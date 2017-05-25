package au.com.vaadinutils.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

public abstract class EntityContextMenu<E> extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	private List<ContextMenuEvent> events = new ArrayList<>();
	protected E targetEntity;

	public E getTargetEntity()
	{
		return targetEntity;
	}

	protected void fireEvents()
	{
		for (ContextMenuEvent event : events)
		{
			event.preContextMenuOpen();
		}
	}

	public void addEvent(final ContextMenuEvent event)
	{
		events.add(event);
	}
}
