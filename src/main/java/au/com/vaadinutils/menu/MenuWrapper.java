package au.com.vaadinutils.menu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

/**
 * because MenuBar and MenuItem have no common ancestor with the addItem method
 * 
 * @author rsutton
 *
 */
public class MenuWrapper
{
	Logger logger = LogManager.getLogger();
	Object innerMenuObject;

	MenuWrapper(MenuBar bar)
	{
		innerMenuObject = bar;

	}

	MenuWrapper(MenuItem item)
	{
		innerMenuObject = item;
	}

	public void addItem(String displayName, boolean addAtTop, Command command)
	{
		if (innerMenuObject instanceof MenuBar)
		{

			final MenuBar menuBar = (MenuBar) innerMenuObject;
			menuBar.addItem(displayName, command);
			logger.debug("for menu " + menuBar.getCaption());
		}
		else
		{
			boolean done = false;
			final MenuItem menuItem = (MenuItem) innerMenuObject;
			if (menuItem.getChildren() != null)
			{
				if (addAtTop)
				{
					menuItem.addItemBefore(displayName, null, command, menuItem.getChildren().get(0));
					done = true;
				}
				else
				{
					for (MenuItem item : menuItem.getChildren())
					{
						if (item.getText().compareToIgnoreCase(displayName) > 0)
						{
							logger.debug("for menu " + menuItem.getText() + " Inserting " + displayName + " before "
									+ item.getText());
							menuItem.addItemBefore(displayName, null, command, item);
							done = true;
							break;
						}

					}
				}
			}
			if (!done)
			{
				menuItem.addItem(displayName, command);
				logger.debug("for menu " + menuItem.getText() + " Inserting " + displayName);

			}

		}

	}

}
