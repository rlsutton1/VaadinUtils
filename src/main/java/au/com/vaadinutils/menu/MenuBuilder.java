package au.com.vaadinutils.menu;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

public class MenuBuilder implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ArrayList<ViewMap> viewMap;
	private Navigator navigator;

	public MenuBuilder(Navigator navigator, ArrayList<ViewMap> viewMap)
	{
		this.navigator = navigator;
		this.viewMap = viewMap;
	}

	public MenuBar build()
	{
		MenuBar menubar = new MenuBar();
		for (final ViewMap viewmap : this.viewMap)
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

					String[] pathElements = path.split("\\.");
					
					if (pathElements.length == 2)
						getMenuItem(menubar, viewmap.getViewName(), menu.display(), pathElements[1], false);
					else
					{
						MenuItem parentMenuItem = getMenuItem(menubar,  viewmap.getViewName(), pathElements[1], pathElements[1], true);
						resursiveAdd(parentMenuItem, viewmap.getViewName(), menu.display(),
								Arrays.copyOfRange(pathElements, 2, pathElements.length));
					}
				}
			}
		}
		return menubar;

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
	 * @param menuName
	 * @param pathElements
	 */
	private void resursiveAdd(MenuItem menuItem, final String viewName, String menuName, String[] pathElements)
	{
		if (pathElements.length > 0)
		{
			if (pathElements.length == 1)
			{
				// Time to insert the actual menu item

				// First see if the item is already on the menubar
				menuItem.addItem(menuName, new MenuBar.Command()
				{
					private static final long serialVersionUID = 1L;

					public void menuSelected(MenuItem selectedItem)
					{
						navigator.navigateTo(viewName);
					}
				});
			}
			else
			{
				// We need to navigate down further
				String currentPath = pathElements[0];
				MenuItem currentItem = getMenuItem(menuItem, currentPath, currentPath);

				resursiveAdd(currentItem, viewName, menuName, Arrays.copyOfRange(pathElements, 1, pathElements.length));

			}
		}

	}

	private MenuItem getMenuItem(MenuItem parentItem, final String menuName, String currentPath)
	{
		MenuItem currentItem = findMenuItem(parentItem.getChildren(), currentPath);
		if (currentItem != null)
		{
			currentItem = parentItem.addItem(menuName, new MenuBar.Command()
			{
				private static final long serialVersionUID = 1L;

				public void menuSelected(MenuItem selectedItem)
				{
					UI.getCurrent().getNavigator().navigateTo(menuName);
				}
			});

		}
		return currentItem;
	}

	private MenuItem getMenuItem(MenuBar parentItem, final String viewName, final String displayName, String currentPath, final boolean parent)
	{
		MenuItem currentItem = findMenuItem(parentItem.getItems(), currentPath);
		if (currentItem == null)
		{
			currentItem = parentItem.addItem(displayName, new MenuBar.Command()
			{
				private static final long serialVersionUID = 1L;

				public void menuSelected(MenuItem selectedItem)
				{
					if (!parent)
						UI.getCurrent().getNavigator().navigateTo(viewName);
				}
			});

		}
		return currentItem;
	}

	private MenuItem findMenuItem(List<MenuItem> list, String currentPath)
	{
		MenuItem currentItem = null;
		for (MenuItem menuItem : list)
		{
			if (menuItem.getText().equals(currentPath))
			{
				currentItem = menuItem;
				break;
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
