package au.com.vaadinutils.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.alump.gridstack.GridStackCoordinates;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
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
	private PortalResizeListener resizeListener;
	Logger logger = LogManager.getLogger();

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

		Button button = createAddButton(title);

		label.setWidth("200");
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
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setHeight("38");

		return layout;

	}

	protected Button createAddButton(final String title)
	{
		Button button = new Button(FontAwesome.PLUS.getHtml());
		button.setDescription("Click to add '" + title + "' to the current Dashboard");

		button.setCaptionAsHtml(true);
		button.setStyleName(ValoTheme.BUTTON_TINY);
		button.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		return button;
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
		if (portal == null)
		{
			logger.warn("NULL portal");
			return;
		}
		Set<Tblportalconfig> removeList = new HashSet<>();
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
		setValue(portal, key, "" + value);

	}

	@Override
	public void setValue(Tblportal portal, String key, String value)
	{
		if (portal != null)
		{
			portal.setConfigValue(key, value);
		}
		else
		{
			logger.error("Call to BasePortalAdder.setValue where portal is null and key =" + key + ", value =" + value);
		}
	}

	@Override
	public String getValueString(Tblportal portal, String key)
	{
		if (portal == null)
		{
			return "";
		}
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
	public double getValueDouble(Tblportal portal, String key)
	{
		try
		{
			return Double.parseDouble(portal.getConfigValue(key, "0.0"));
		}
		catch (Exception e)
		{
			return 0.0;
		}

	}

	@Override
	public void savePosition(Tblportal portal, GridStackCoordinates event)
	{
		setValue(portal, "X", event.getX());
		setValue(portal, "Y", event.getY());
		setValue(portal, "Width", event.getWidth());
		setValue(portal, "Height", event.getHeight());
		if (resizeListener != null)
		{
			resizeListener.portalResized(event);
		}

	}

	@Override
	public void addResizeListener(PortalResizeListener resizeListener)
	{
		this.resizeListener = resizeListener;

	}

	@Override
	public void addCustomHeaderButtons(HorizontalLayout controlLayout)
	{

	}

	@Override
	public Collection<String> getKeys(Tblportal portal)
	{
		Set<String> keys = new HashSet<>();
		for (Tblportalconfig config : portal.getConfigs())
		{
			keys.add(config.getKey());
		}
		return keys;
	}

}
