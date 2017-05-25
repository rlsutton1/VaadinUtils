package au.com.vaadinutils.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

public class LabelContextMenu extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	private List<ContextMenuEvent> events = new ArrayList<>();

	public void addEvent(final ContextMenuEvent event)
	{
		events.add(event);
	}

}
