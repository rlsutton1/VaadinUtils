package au.com.vaadinutils.menu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

public enum ActionType
{
	NAVIGATE
	{
		@Override
		public void createLeafItem(MenuWrapper menubar, Menu menu, String displayName, final ViewMapping viewmap,
				boolean addAtTop)
		{

			menubar.addItem(displayName, addAtTop, new MenuBar.Command()
			{
				private static final long serialVersionUID = 1L;

				public void menuSelected(MenuItem selectedItem)
				{
					UI.getCurrent().getNavigator().navigateTo(viewmap.getViewName());
				}
			});
		}
	},
	URL
	{
		@Override
		public void createLeafItem(MenuWrapper menubar, final Menu menu, String displayName, ViewMapping viewmap,
				boolean addAtTop)
		{
			menubar.addItem(displayName, addAtTop, new MenuBar.Command()
			{
				private static final long serialVersionUID = 1L;

				public void menuSelected(MenuItem selectedItem)
				{
					UI.getCurrent().getPage().open(menu.url(), "");
				}
			});

		}
	},
	URL_NEW_WINDOW
	{
		@Override
		public void createLeafItem(MenuWrapper menubar, final Menu menu, String displayName, ViewMapping viewmap,
				boolean addAtTop)
		{
			menubar.addItem(displayName, addAtTop, new MenuBar.Command()
			{
				private static final long serialVersionUID = 1L;

				public void menuSelected(MenuItem selectedItem)
				{
					if (menu.windowSizer() != WindowSizerNull.class)
					{
						// dynamically sized window!
						try
						{
							WindowSizer sizer = menu.windowSizer().newInstance();
							UI.getCurrent()
									.getPage()
									.open(menu.url(), menu.windowName(), sizer.width(), sizer.height(),
											BorderStyle.DEFAULT);
						}
						catch (InstantiationException | IllegalAccessException e)
						{
							logger.error(e, e);
						}

					}
					else if (menu.width() > 0 && menu.height() > 0)
					{
						UI.getCurrent().getPage()
								.open(menu.url(), menu.windowName(), menu.width(), menu.height(), BorderStyle.DEFAULT);

					}
					else
					{
						UI.getCurrent().getPage().open(menu.url(), menu.windowName());
					}
				}
			});

		}
	};

	Logger logger = LogManager.getLogger();

	abstract public void createLeafItem(MenuWrapper menuItem, Menu menu, final String displayName,
			final ViewMapping viewmap, boolean addAtTop);

}
