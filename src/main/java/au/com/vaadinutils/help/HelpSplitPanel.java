package au.com.vaadinutils.help;

import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.SliderPanelStyles;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderPanelListener;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import au.com.vaadinutils.errorHandling.ErrorWindow;
import au.com.vaadinutils.user.UserSettingsStorage;
import au.com.vaadinutils.user.UserSettingsStorageFactory;

public class HelpSplitPanel extends HorizontalLayout implements View, HelpPageListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5011140025383708388L;
	private Panel helpPane;
	private VerticalLayout helpHolder = new VerticalLayout();

	HelpIndex index = HelpIndexFactory.getHelpIndex();

	View component;
	private Enum<?> currentHelpId;
	private Button hideButton;
	UserSettingsStorage userSettings = UserSettingsStorageFactory.getUserSettingsStorage();

	private SliderPanel helpSliderPanel;

	public View getActiveView()
	{
		return component;
	}

	public HelpSplitPanel(View component)
	{
		super();

		this.component = component;
		if (component instanceof HelpPageListenerMinimal)
		{
			((HelpPageListenerMinimal) component).setHelpPageListener(this);
		}

		setImmediate(true);

		buildMainLayout();
		setSizeFull();
		((Component) component).setSizeFull();

		Enum<?> helpId = ((HelpProvider) component).getHelpId();
		setHelpPageId(helpId);

		this.setImmediate(true);

	}

	public View getView()
	{
		return component;
	}

	boolean helpHiding = false;
	private VerticalLayout innerSecondPanel;

	@Override
	public void enter(ViewChangeEvent event)
	{
		// allow the View we are wrapping to process the enter event in the
		// normal way
		try
		{
			component.enter(event);
		}
		catch (Exception e)
		{
			ErrorWindow.showErrorWindow(e);
		}
	}

	public void setHelpPageId(Enum<?> helpId)
	{
		setHelp(helpId);

	}

	private void buildMainLayout()
	{

		addComponent((Component) component);
		setExpandRatio((Component) component, 1);
		helpPane = new Panel();
		helpPane.setImmediate(false);

		showHelpLoadingSplash();

		helpSliderPanel = new SliderPanelBuilder(helpPane).expanded(false).mode(SliderMode.RIGHT)
				.tabPosition(SliderTabPosition.MIDDLE).style(SliderPanelStyles.COLOR_BLUE).caption("Help")
				.animationDuration(400).tabSize(30).autoCollapseSlider(true)
				.fixedContentSize((int) (UI.getCurrent().getPage().getBrowserWindowWidth() * 0.75)).build();

		helpLoader = new SlideOutLoader();

		innerSecondPanel = new VerticalLayout();
		innerSecondPanel.setSizeFull();
		innerSecondPanel.setWidth("30");
		innerSecondPanel.addComponent(helpSliderPanel);
		innerSecondPanel.setComponentAlignment(helpSliderPanel, Alignment.MIDDLE_RIGHT);

		addComponent(innerSecondPanel);

		Page.getCurrent().addBrowserWindowResizeListener(new BrowserWindowResizeListener()
		{

			private static final long serialVersionUID = -8548907013566961812L;

			@Override
			public void browserWindowResized(BrowserWindowResizeEvent event)
			{

				helpSliderPanel.setFixedContentSize((int) (event.getWidth() * 0.75));
				if (helpSliderPanel.isExpanded())
				{
					helpSliderPanel.collapse();
				}

			}

		});

	}

	private void showHelpLoadingSplash()
	{
		VerticalLayout splashHolder = new VerticalLayout();
		splashHolder.setSizeFull();
		Label splashLabel = new Label("<h2><br><br><b><center>Loading help, Please wait...<b>", ContentMode.HTML);
		splashHolder.addComponent(splashLabel);
		splashHolder.setComponentAlignment(splashLabel, Alignment.MIDDLE_RIGHT);

		helpPane.setContent(splashHolder);
		helpPane.setSizeFull();
	}

	private void resizeHelp()
	{

		helpPane.setSizeFull();
		helpHolder.setSizeFull();

	}

	@Override
	public void removeAllComponents()
	{
		throw new RuntimeException("Cant remove compoents this way");
	}

	@Override
	public void removeComponent(Component component)
	{
		throw new RuntimeException("Cant remove compoents this way");
	}

	boolean showHelpOnPage = false;

	interface HelpLoader
	{
		void showHelp(final Enum<?> helpId, VerticalLayout helpHolder, HelpDisplayedCallback callback);
	}

	HelpLoader helpLoader;

	class SlideOutLoader implements HelpLoader
	{
		boolean loaded = false;
		private Enum<?> helpId;
		private HelpDisplayedCallback callback;

		SlideOutLoader()
		{
			helpSliderPanel.addListener(new SliderPanelListener()
			{

				@Override
				public void onToggle(boolean expand)
				{
					if (expand && !loaded)
					{
						if (helpId != null && callback != null)
						{
							index.setHelpSource(helpId, helpHolder, callback);
							loaded = true;
						}
					}

				}
			});

		}

		@Override
		public void showHelp(Enum<?> helpId, VerticalLayout helpHolder, HelpDisplayedCallback callback)
		{
			loaded = false;
			this.helpId = helpId;
			this.callback = callback;
			if (helpSliderPanel.isExpanded())
			{
				index.setHelpSource(helpId, helpHolder, callback);
				loaded = true;
			}
		}

	}

	class OnLayoutLoader implements HelpLoader
	{

		@Override
		public void showHelp(Enum<?> helpId, VerticalLayout helpHolder, HelpDisplayedCallback callback)
		{
			index.setHelpSource(helpId, helpHolder, callback);
		}

	}

	@Override
	public void setHelp(final Enum<?> helpId)
	{

		currentHelpId = helpId;

		helpHolder = new VerticalLayout();

		helpHolder.setMargin(true);

		helpLoader.showHelp(helpId, helpHolder, new HelpDisplayedCallback()
		{

			@Override
			public void success()
			{
				helpPane.setContent(helpHolder);
				addFooter();
				resizeHelp();

			}

			@Override
			public void fail()
			{
				helpPane.setContent(helpHolder);
				addFooter();
				resizeHelp();

			}

			private void addFooter()
			{
				hideButton = new Button("Show Help On Page");
				hideButton.setStyleName(Reindeer.BUTTON_SMALL);
				hideButton.addClickListener(new ClickListener()
				{

					private static final long serialVersionUID = -521965129353754937L;

					@Override
					public void buttonClick(ClickEvent event)
					{
						showHelpOnPage();

					}

				});

				HorizontalLayout helpFooter = new HorizontalLayout();
				helpFooter.setWidth("100%");
				helpFooter.setSpacing(true);

				if (!showHelpOnPage)
				{

					Button closeButton = new Button("Close");
					closeButton.addClickListener(new ClickListener()
					{

						private static final long serialVersionUID = 8822575654757513760L;

						@Override
						public void buttonClick(ClickEvent event)
						{
							if (helpSliderPanel != null)
							{
								helpSliderPanel.collapse();
							}

						}
					});

					helpFooter.addComponent(closeButton);
					helpFooter.setComponentAlignment(closeButton, Alignment.BOTTOM_LEFT);

					helpFooter.addComponent(hideButton);
					helpFooter.addComponent(new Label("Help id is " + helpId));

					helpFooter.setComponentAlignment(hideButton, Alignment.BOTTOM_RIGHT);

					closeButton.setStyleName(Reindeer.BUTTON_SMALL);

				}
				helpHolder.addComponent(helpFooter);

				helpHolder.setComponentAlignment(helpFooter, Alignment.BOTTOM_LEFT);
				// helpPane.setExpandRatio(helpLabel, .1f);

			}

			@Override
			public void showHelpLoadingSplash()
			{
				HelpSplitPanel.this.showHelpLoadingSplash();

			}

		});

	}

	@Override
	public void setHelpPageListener(HelpPageListener helpSplitPanel)
	{
		throw new RuntimeException(
				"This is the top level HelpPageListenerMinimal, you cant set the HelpPageListenerMinimal");

	}

	@Override
	public void showHelpOnPage()
	{

		helpLoader = new OnLayoutLoader();
		showHelpOnLayout(innerSecondPanel);
		innerSecondPanel.setWidth("" + (UI.getCurrent().getPage().getBrowserWindowWidth() * 0.5));

	}

	@Override
	public void showHelpOnLayout(VerticalLayout layout)
	{
		helpLoader = new OnLayoutLoader();
		// innerSecondPanel.removeComponent(helpPane);
		final VerticalLayout content = new VerticalLayout(new Label("Help is current displayed on the page"));
		content.setMargin(true);
		helpSliderPanel.setContent(content);
		helpSliderPanel.setVisible(false);
		layout.addComponent(helpPane);
		showHelpOnPage = true;
		setHelpPageId(currentHelpId);

		resizeHelp();

	}

	@Override
	public void resetHelpPosition()
	{
		helpLoader = new SlideOutLoader();
		innerSecondPanel.setWidth("30");
		helpSliderPanel.setContent(helpPane);
		helpSliderPanel.setVisible(true);
		showHelpOnPage = false;
		setHelpPageId(currentHelpId);
		resizeHelp();

	}

}
