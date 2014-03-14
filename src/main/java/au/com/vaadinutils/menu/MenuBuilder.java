package au.com.vaadinutils.menu;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

public class MenuBuilder implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ArrayList<ViewMapping> viewMap;
	private Navigator navigator;

	public MenuBuilder(Navigator navigator, ArrayList<ViewMapping> viewMap)
	{
		this.navigator = navigator;
		this.viewMap = viewMap;
	}

	public MenuBar build()
	{
		MenuBar menubar = new MenuBar();
		for (final ViewMapping viewmap : this.viewMap)
		{
			// We don't add a menu item from the default view.
			if (!viewmap.getViewName().equals(""))
			{
				Menu menu = getMenuAnnotation(viewmap.getView());
				if (menu != null)
				{
					String path = menu.path();

					// All menus should start with the MENUBAR prefix but we
					// make it
					// optional here.
					if (!path.startsWith(Menu.MENUBAR))
					{
						path = Menu.MENUBAR + "." + path;
					}

					// Append the menu item name to the end of the path
					path += "." + menu.display();

					final String[] pathElements = path.split("\\.");

					if (pathElements.length == 2)
					{
						createLeafItem(menubar, menu.display(), viewmap);
					}
					else
					{
						MenuItem parentMenuItem = getParentMenuItem(menubar, pathElements[1]);// ,
																								// pathElements[1],
																								// pathElements[1],
																								// true);
						resursiveAdd(parentMenuItem, viewmap, menu.display(),
								Arrays.copyOfRange(pathElements, 2, pathElements.length));
					}
				}
			}
		}
		return menubar;

	}

	private void createLeafItem(MenuBar menubar, final String displayName, final ViewMapping viewmap)
	{
		menubar.addItem(displayName, new MenuBar.Command()
		{
			private static final long serialVersionUID = 1L;

			public void menuSelected(MenuItem selectedItem)
			{
				UI.getCurrent().getNavigator().navigateTo(viewmap.getViewName());
			}
		});
	}

	/**
	 * Navigate down the menu hierarchy until we find the right sport to add the
	 * menu item.
	 * 
	 * We create the menu structure as we go if needed.
	 * 
	 * @param menuItem
	 * 
	 * @param menubar
	 * @param displayName
	 * @param pathElements
	 */
	private void resursiveAdd(MenuItem menuItem, final ViewMapping viewmap, String displayName, String[] pathElements)
	{
		if (pathElements.length > 0)
		{
			if (pathElements.length == 1)
			{
				// Time to insert the actual menu item

				// First see if the item is already on the menubar
				createLeafItem(menuItem, displayName, viewmap);
			}
			else
			{
				// We need to navigate down further
				String currentPath = pathElements[0];
				MenuItem currentItem = getMenuItem(menuItem, currentPath); // ,
																			// currentPath);

				Preconditions.checkNotNull(currentItem);

				resursiveAdd(currentItem, viewmap, displayName, Arrays.copyOfRange(pathElements, 1, pathElements.length));

			}
		}

	}

	private void createLeafItem(MenuItem menuItem, String displayName, final ViewMapping viewmap)
	{
		menuItem.addItem(displayName, new MenuBar.Command()
		{
			private static final long serialVersionUID = 1L;

			public void menuSelected(MenuItem selectedItem)
			{
				navigator.navigateTo(viewmap.getViewName());
			}
		});
	}

	/**
	 * Searches for a menu item. If it doesn't exist it will be created.
	 * 
	 * @param parentItem
	 * @param displayName
	 * @param currentPath
	 * @return
	 */
	private MenuItem getMenuItem(MenuItem parentItem, final String displayName) // ,
																				// String
																				// currentPath)
	{
		MenuItem currentItem = findMenuItem(parentItem.getChildren(), displayName);

		if (currentItem == null)
		{
			currentItem = parentItem.addItem(displayName, null);
		}
		// if (currentItem != null)
		// {
		// currentItem = parentItem.addItem(menuName, new MenuBar.Command()
		// {
		// private static final long serialVersionUID = 1L;
		//
		// public void menuSelected(MenuItem selectedItem)
		// {
		// UI.getCurrent().getNavigator().navigateTo(menuName);
		// }
		// });
		//
		// }
		return currentItem;
	}

	/**
	 * Searches for a parement menu item. If it doesn't exist it will be
	 * created.
	 * 
	 * @param parentItem
	 * @param menuName
	 * @param currentPath
	 * @return
	 */
	private MenuItem getParentMenuItem(MenuBar parentItem, final String displayName) // ,
																				// String
																				// currentPath)
	{
		MenuItem currentItem = findMenuItem(parentItem.getItems(), displayName);
		if (currentItem == null)
		{
			currentItem = parentItem.addItem(displayName, null);
			/*
			 * , new MenuBar.Command() { private static final long
			 * serialVersionUID = 1L;
			 * 
			 * public void menuSelected(MenuItem selectedItem) { if (!parent)
			 * UI.getCurrent().getNavigator().navigateTo(viewName); } });
			 */

		}
		return currentItem;
	}

	private MenuItem findMenuItem(List<MenuItem> list, String currentPath)
	{
		MenuItem currentItem = null;

		if (list != null)
		{
			for (MenuItem menuItem : list)
			{
				if (menuItem.getText().equals(currentPath))
				{
					currentItem = menuItem;
					break;
				}
			}
		}
		return currentItem;
	}

	private Menu getMenuAnnotation(Class<? extends View> viewClass)
	{
		Menu menu = null;
		Class<? extends View> aClass = viewClass;
		Annotation annotation = aClass.getAnnotation(Menu.class);

		if (annotation instanceof Menu)
		{
			menu = (Menu) annotation;
		}
		return menu;
	}

}
