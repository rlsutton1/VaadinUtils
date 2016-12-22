package au.com.vaadinutils.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.GridContextClickEvent;

public class GridContextMenu<E> extends EntityContextMenu<E>
{
	private static final long serialVersionUID = 1L;

	private List<ContextMenuEvent> eventsList = new ArrayList<>();
	private Grid grid;

	/**
	 * Assigns this as the context menu of given table. Allows context menu to
	 * appear only on certain parts of the table.
	 *
	 * @param table
	 * @param onRow
	 *            show context menu when row is clicked
	 * @param onHeader
	 *            show context menu when header is clicked
	 * @param onFooter
	 *            show context menu when footer is clicked
	 */
	public void setAsGridContextMenu(final Grid grid, final boolean onRow, final boolean onHeader,
			final boolean onFooter)
	{
		this.grid = grid;
		extend(grid);
		setOpenAutomatically(false);

		grid.addContextClickListener(new ContextClickListener()
		{
			private static final long serialVersionUID = -2197393292360426242L;

			@Override
			public void contextClick(ContextClickEvent event)
			{
				if (!(event instanceof GridContextClickEvent))
				{
					return;
				}

				final GridContextClickEvent e = (GridContextClickEvent) event;
				switch (e.getSection())
				{
					case BODY:
						if (onRow)
						{
							openContext(e);
						}
						break;
					case FOOTER:
						if (onFooter)
						{
							openContext(e);
						}
						break;
					case HEADER:
						if (onHeader)
						{
							openContext(e);
						}
						break;
					default:
						break;
				}
			}
		});
	}

	private void openContext(final GridContextClickEvent event)
	{
		try
		{
			@SuppressWarnings("unchecked")
			final E itemId = (E) event.getItemId();
			if (itemId == null)
			{
				return;
			}

			targetEntity = itemId;
			grid.select(itemId);

			for (ContextMenuEvent events : eventsList)
			{
				events.preContextMenuOpen();
			}
			open(event.getClientX(), event.getClientY());
		}
		catch (IllegalArgumentException e)
		{
			// This usually means we have tried to select something that doesn't
			// exist in the grid. This can happen when trying to open a context
			// menu on old items while the grid is still refreshing with new
			// items.
		}
	}

	public void addEvents(final ContextMenuEvent events)
	{
		eventsList.add(events);
	}
}
