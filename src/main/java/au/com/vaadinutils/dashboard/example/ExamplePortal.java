package au.com.vaadinutils.dashboard.example;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.dashboard.BasePortal;
import au.com.vaadinutils.dashboard.BasePortalAdder;
import au.com.vaadinutils.dashboard.DashBoardController;
import au.com.vaadinutils.dashboard.Portal;
import au.com.vaadinutils.dashboard.PortalConfigDelgate;
import au.com.vaadinutils.dashboard.Tblportal;
import au.com.vaadinutils.editors.InputDialog;
import au.com.vaadinutils.editors.Recipient;

public class ExamplePortal extends BasePortalAdder
{

	public ExamplePortal(String portalLayoutGuid)
	{
		super(portalLayoutGuid);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getTitle()
	{
		return "Example title";
	}

	@Override
	protected Enum<?> getPortalEnum()
	{
		return ExampleDashboardEnum.EXAMPLE;
	}

	@Override
	protected Portal instancePortal(DashBoardController dashBoard, Tblportal portal)
	{
		return new IFramePortal(portal, dashBoard, this);

	}

	final static class IFramePortal extends BasePortal
	{
		private static final long serialVersionUID = 1L;
		final BrowserFrame iFrame = new BrowserFrame();

		public IFramePortal(Tblportal portal, DashBoardController dashBoard, PortalConfigDelgate configDelegate)
		{
			super(portal, dashBoard, configDelegate);

			iFrame.setSizeFull();

			String url = getConfigDelegate().getValueString(portal, "url");
			if (StringUtils.isNotEmpty(url))
			{
				iFrame.setSource(new ExternalResource(url));

			}
			else
			{
				iFrame.setSource(new ExternalResource("http://www.bom.gov.au/products/IDR023.loop.shtml#skip"));
			}
			iFrame.setSizeFull();

			addComponent(iFrame);
			setExpandRatio(iFrame, 1.0f);
			setSizeFull();

		}

		@Override
		protected void addCustomHeaderButtons(HorizontalLayout controlLayout)
		{
			Button config = new Button(FontAwesome.COG);
			config.setStyleName(ValoTheme.BUTTON_LINK);
			config.addClickListener(new ClickListener()
			{

				private static final long serialVersionUID = -2021410912212917005L;

				@Override
				public void buttonClick(ClickEvent event)
				{
					InputDialog dialog = new InputDialog(UI.getCurrent(), "Set Web Address", "Web Address?",
							new Recipient()
							{

								@Override
								public boolean onOK(String input)
								{
									iFrame.setSource(new ExternalResource(input));
									getConfigDelegate().setValue(getPortal(), "url", input);

									return true;
								}

								@Override
								public boolean onCancel()
								{
									return true;
								}
							});
					String url = getConfigDelegate().getValueString(getPortal(), "url");
					if (StringUtils.isNotEmpty(url))
					{
						dialog.setDefaultValue(url);

					}

					dialog.setWidth("700");
					dialog.setHeight("250");

				}
			});

			controlLayout.addComponent(config);

		}

	}
}
