package au.com.vaadinutils.dashboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vaadin.alump.gridstack.GridStackCoordinates;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;

public abstract class BasePortalAdder extends VerticalLayout implements PortalConfigDelgate, PortalAdderIfc
{
	private static final long serialVersionUID = 1L;
	private String portalLayoutGuid;

	public BasePortalAdder(String portalLayoutGuid)
	{
		this.portalLayoutGuid = portalLayoutGuid;
	}

	@Override
	public Component getVaadinAddLayout(final DashBoardController dashBoard, final DashBoardView view)
	{
		final String title = getTitle();

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		Label label = new Label(title);

		Button button = new Button(FontAwesome.PLUS.getHtml());
		button.setCaptionAsHtml(true);
		button.setStyleName(ValoTheme.BUTTON_TINY);
		label.setWidth("300");
		button.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				Tblportallayout layout = JpaBaseDao.getGenericDao(Tblportallayout.class)
						.findOneByAttribute(Tblportallayout_.guid, portalLayoutGuid);
				Tblportal portal = new Tblportal();
				layout.addPortal(portal);
				portal.setType(getPortalEnum().toString());
				EntityManagerProvider.persist(portal);

				addPortal(dashBoard, portal);
				view.closeToolBar();
			}
		});

		layout.addComponent(button);
		layout.addComponent(label);

		return layout;

	}

	protected abstract Enum<?> getPortalEnum();

	@Override
	public void addPortal(final DashBoardController dashBoard, Tblportal portal)
	{

		Portal component = instancePortal(dashBoard, portal);

		dashBoard.addComponent(component);
		Integer x = getValueInt(portal, "X", null);
		Integer y = getValueInt(portal, "Y", null);
		Integer width = getValueInt(portal, "Width", getDefaultWidth());
		Integer height = getValueInt(portal, "Height", getDefaultHeight());

		dashBoard.moveAndResizeComponent(component, x, y, width, height);
	}

	protected int getDefaultWidth()
	{
		return 3;
	}

	protected int getDefaultHeight()
	{
		return 5;
	}

	protected abstract Portal instancePortal(final DashBoardController dashBoard, Tblportal portal);

	@Override
	public Map<String, Integer> getValuesLikeInt(Tblportal portal, String string)
	{
		Map<String, Integer> values = new HashMap<>();
		for (Tblportalconfig config : portal.getConfigs())
		{
			if (config.getKey().startsWith(string))
			{
				values.put(config.getKey(), Integer.parseInt(config.getValue()));
			}
		}
		return values;
	}

	@Override
	public void deleteValuesLike(Tblportal portal, String baseKey)
	{
		Set<Tblportalconfig> removeList = new HashSet<>();
		Map<String, Integer> values = new HashMap<>();
		for (Tblportalconfig config : portal.getConfigs())
		{
			if (config.getKey().startsWith(baseKey))
			{
				removeList.add(config);
			}
		}

		for (Tblportalconfig key : removeList)
		{
			portal.removeConfig(key);
			EntityManagerProvider.remove(key);
		}

	}

	@Override
	public void setValue(Tblportal portal, String key, int value)
	{
		portal.setConfigValue(key, "" + value);

	}

	@Override
	public void setValue(Tblportal portal, String key, String value)
	{
		portal.setConfigValue(key, value);

	}

	@Override
	public String getValueString(Tblportal portal, String key)
	{
		return portal.getConfigValue(key, "");

	}

	@Override
	public Integer getValueInt(Tblportal portal, String key, Integer defaultValue)
	{
		try
		{
			return Integer.parseInt(portal.getConfigValue(key, "" + defaultValue));
		}
		catch (Exception e)
		{
			return defaultValue;
		}

	}

	@Override
	public void savePosition(Tblportal portal, GridStackCoordinates event)
	{
		setValue(portal, "X", event.getX());
		setValue(portal, "Y", event.getY());
		setValue(portal, "Width", event.getWidth());
		setValue(portal, "Height", event.getHeight());

	}

	@Override
	public void addCustomHeaderButtons(HorizontalLayout controlLayout)
	{

	}

}
