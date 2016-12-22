package au.com.vaadinutils.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.ui.Button;

public class ButtonContextMenu<E> extends EntityContextMenu<E>
{
	private static final long serialVersionUID = 1L;

	private List<ContextMenuEvent> eventsList = new ArrayList<>();

	/**
	 * Assigns this as the context menu of a button.
	 */
	public void setAsButtonContextMenu(final Button button, final E targetEntity)
	{
		this.targetEntity = targetEntity;
		extend(button);
		setOpenAutomatically(false);

		button.addContextClickListener(new ContextClickListener()
		{

			private static final long serialVersionUID = 4777762727944373063L;

			@Override
			public void contextClick(ContextClickEvent event)
			{
				openContext(event);
			}
		});
	}

	private void openContext(final ContextClickEvent event)
	{
		for (ContextMenuEvent events : eventsList)
		{
			events.preContextMenuOpen();
		}

		open(event.getClientX(), event.getClientY());
	}

	public void addEvents(final ContextMenuEvent events)
	{
		eventsList.add(events);
	}
}
