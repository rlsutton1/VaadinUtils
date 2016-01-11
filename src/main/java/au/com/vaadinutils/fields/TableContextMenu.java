package au.com.vaadinutils.fields;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.FooterClickEvent;
import com.vaadin.ui.Table.FooterClickListener;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Table.HeaderClickListener;

public class TableContextMenu extends ContextMenu
{
	private static final long serialVersionUID = 1L;

	private TableContextMenuEvents events;

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
	public void setAsTableContextMenu(final Table table, final boolean onRow, final boolean onHeader,
			final boolean onFooter)
	{
		extend(table);

		setOpenAutomatically(false);

		if (onRow)
		{
			table.addItemClickListener(new ItemClickEvent.ItemClickListener()
			{
				private static final long serialVersionUID = -348059189217149508L;

				@Override
				public void itemClick(ItemClickEvent event)
				{
					if (event.getButton() == MouseButton.RIGHT)
					{
						events.preContextMenuOpen();
						fireEvent(new ContextMenuOpenedOnTableRowEvent(TableContextMenu.this, table, event.getItemId(),
								event.getPropertyId()));
						open(event.getClientX(), event.getClientY());
					}
				}
			});
		}

		if (onHeader)
		{
			table.addHeaderClickListener(new HeaderClickListener()
			{
				private static final long serialVersionUID = -5880755689414670581L;

				@Override
				public void headerClick(HeaderClickEvent event)
				{
					if (event.getButton() == MouseButton.RIGHT)
					{
						events.preContextMenuOpen();
						fireEvent(new ContextMenuOpenedOnTableHeaderEvent(TableContextMenu.this, table, event
								.getPropertyId()));
						open(event.getClientX(), event.getClientY());
					}
				}
			});
		}

		if (onFooter)
		{
			table.addFooterClickListener(new FooterClickListener()
			{
				private static final long serialVersionUID = 2884227013964132482L;

				@Override
				public void footerClick(FooterClickEvent event)
				{
					if (event.getButton() == MouseButton.RIGHT)
					{
						events.preContextMenuOpen();
						fireEvent(new ContextMenuOpenedOnTableHeaderEvent(TableContextMenu.this, table, event
								.getPropertyId()));
						open(event.getClientX(), event.getClientY());
					}
				}
			});
		}
	}

	public void setEvents(TableContextMenuEvents events)
	{
		this.events = events;
	}
}
