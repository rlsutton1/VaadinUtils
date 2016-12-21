package au.com.vaadinutils.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

public class LabelContextMenu extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	private List<ContextMenuEvent> eventsList = new ArrayList<ContextMenuEvent>();

	public void addEvents(final ContextMenuEvent events)
	{
		eventsList.add(events);
	}

}
