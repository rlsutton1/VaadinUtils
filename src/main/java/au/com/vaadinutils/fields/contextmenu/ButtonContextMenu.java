package au.com.vaadinutils.fields.contextmenu;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.ui.Button;

public class ButtonContextMenu<E> extends EntityContextMenu<E>
{
	private static final long serialVersionUID = 1L;

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
				openContext(event.getClientX(), event.getClientY());
			}
		});
	}

	public void openContext(final com.vaadin.ui.Button.ClickEvent event)
	{
		openContext(event.getClientX(), event.getClientY());
	}

	private void openContext(final int clientX, final int clientY)
	{
		// Make sure we have an up to date copy of the entity from the db
		targetEntity = loadEntity(targetEntity);

		fireEvents();
		open(clientX, clientY);
	}
}
