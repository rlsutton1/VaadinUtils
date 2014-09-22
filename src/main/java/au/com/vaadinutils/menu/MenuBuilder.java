package au.com.vaadinutils.menu;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudSecurityManager;
import au.com.vaadinutils.crud.security.SecurityManagerFactoryProxy;

import com.google.common.base.Preconditions;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class MenuBuilder implements Serializable
{
	private static final long serialVersionUID = 1L;
	private List<ViewMapping> viewMap;
	
	Logger logger = LogManager.getLogger();

	public MenuBuilder(Navigator navigator, List<ViewMapping> viewMap)
	{
		this.viewMap = viewMap;
	}

	public MenuBar build()
	{
		return build(new LinkedList<String>());
	}

	/**
	 * 
	 * @param topLevelMenuOrder
	 *            - specify the order of the top level menus
	 * @return
	 */
	public MenuBar build(List<String> topLevelMenuOrder)
	{

		MenuBar menubar = new MenuBar();

		// used to track top level menus added that have no children attached,
		// they will be removed at the end.
		Set<MenuItem> unusedTopLevelMenus = new HashSet<>();
		for (String menuName : topLevelMenuOrder)
		{
			MenuItem item = menubar.addItem(menuName, null);
			unusedTopLevelMenus.add(item);
		}

		for (final ViewMapping viewmap : this.viewMap)
		{
			// We don't add a menu item from the default view.
			if (!viewmap.getViewName().equals(""))
			{
				CrudSecurityManager model = SecurityManagerFactoryProxy.getSecurityManager(viewmap.getView());
				if (model.canUserView())
				{
					
					addMenuItems(menubar, unusedTopLevelMenus, viewmap);
				}
			}
		}

		// remove unused top level menus
		for (MenuItem item : unusedTopLevelMenus)
		{
			menubar.removeItem(item);
		}
		return menubar;

	}

	private void addMenuItems(MenuBar menubar, Set<MenuItem> unusedTopLevelMenus, final ViewMapping viewmap)
	{
		for (Menu menu : getMenuAnnotations(viewmap.getView()))
		{
			String path = createMenuPathString(menu);

			final String[] pathElements = path.split("\\.");

			if (pathElements.length == 2)
			{
				menu.actionType().createLeafItem(new MenuWrapper(menubar), menu, menu.display(), viewmap,
						menu.atTop());
			}
			else
			{
				MenuItem parentMenuItem = getParentMenuItem(menubar, pathElements[1]);
				unusedTopLevelMenus.remove(parentMenuItem);
				recursiveAdd(parentMenuItem, menu, viewmap, menu.display(),
						Arrays.copyOfRange(pathElements, 2, pathElements.length));
			}
		}
	}

	private String createMenuPathString(Menu menu)
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
		return path;
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
	private void recursiveAdd(MenuItem menuItem, Menu menu, final ViewMapping viewmap, String displayName,
			String[] pathElements)
	{
		if (pathElements.length > 0)
		{
			if (pathElements.length == 1)
			{
				// Time to insert the actual menu item

				// First see if the item is already on the menubar
				menu.actionType()
						.createLeafItem(new MenuWrapper(menuItem), menu, menu.display(), viewmap, menu.atTop());
			}
			else
			{
				// We need to navigate down further
				String currentPath = pathElements[0];
				MenuItem currentItem = getMenuItem(menuItem, currentPath); // ,
																			// currentPath);

				Preconditions.checkNotNull(currentItem);

				recursiveAdd(currentItem, menu, viewmap, displayName,
						Arrays.copyOfRange(pathElements, 1, pathElements.length));

			}
		}

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

	private List<Menu> getMenuAnnotations(Class<? extends View> viewClass)
	{
		List<Menu> menus = new LinkedList<Menu>();
		Class<? extends View> aClass = viewClass;
		Annotation annotation = aClass.getAnnotation(Menu.class);

		if (annotation instanceof Menu)
		{
			menus.add((Menu) annotation);
		}
		annotation = aClass.getAnnotation(Menus.class);

		if (annotation instanceof Menus)
		{
			for (Menu menu : ((Menus) annotation).menus())
			{
				menus.add(menu);
			}
		}
		return menus;
	}

}
