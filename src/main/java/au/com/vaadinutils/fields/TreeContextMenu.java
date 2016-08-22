package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeContextClickEvent;

public class TreeContextMenu extends ContextMenu
{
	private static final long serialVersionUID = 1L;
	private List<TreeContextMenuEvent> eventList = new ArrayList<>();
	private Tree tree;

	@Override
	public void setAsTreeContextMenu(final Tree tree)
	{
		this.tree = tree;
		extend(tree);
		setOpenAutomatically(false);

		tree.addContextClickListener(new ContextClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void contextClick(ContextClickEvent event)
			{
				if (event.getButton() == MouseButton.RIGHT)
				{
					openContext((TreeContextClickEvent) event);
					open(event.getClientX(), event.getClientY());
				}
			}
		});

	}

	private void openContext(final TreeContextClickEvent event)
	{
		tree.select(event.getItemId());
		for (TreeContextMenuEvent e : eventList)
		{
			e.preContextMenuOpen();
		}
		fireEvent(new ContextMenuOpenedOnTreeItemEvent(this, tree, event.getItemId()));
		open(event.getClientX(), event.getClientY());
	}

	public void addEvent(final TreeContextMenuEvent event)
	{
		eventList.add(event);
	}
}
