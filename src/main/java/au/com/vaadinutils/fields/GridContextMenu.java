package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.GridContextClickEvent;

public class GridContextMenu extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	private List<GridContextMenuEvent> eventsList = new ArrayList<>();
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
					return;

				final GridContextClickEvent e = (GridContextClickEvent) event;
				switch (e.getSection())
				{
				case BODY:
					if (onRow)
						openContext(e);
					break;
				case FOOTER:
					if (onFooter)
						openContext(e);
					break;
				case HEADER:
					if (onHeader)
						openContext(e);
					break;
				default:
					break;
				}
			}
		});
	}

	private void openContext(final GridContextClickEvent event)
	{
		grid.select(event.getItemId());
		for (GridContextMenuEvent events : eventsList)
			events.preContextMenuOpen();
		open(event.getClientX(), event.getClientY());
	}

	public void addEvents(final GridContextMenuEvent events)
	{
		eventsList.add(events);
	}
}
