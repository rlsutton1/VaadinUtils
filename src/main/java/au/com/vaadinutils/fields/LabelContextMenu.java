package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

public class LabelContextMenu extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	private List<LabelContextMenuEvents> eventsList = new ArrayList<LabelContextMenuEvents>();

	public void addEvents(final LabelContextMenuEvents events)
	{
		eventsList.add(events);
	}

}
