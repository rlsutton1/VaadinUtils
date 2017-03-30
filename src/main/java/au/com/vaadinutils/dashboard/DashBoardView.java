package au.com.vaadinutils.dashboard;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.alump.gridstack.GridStackLayoutNoJQuery;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.SliderPanelStyles;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.JpaDslBuilder;
import au.com.vaadinutils.editors.InputFormDialog;
import au.com.vaadinutils.editors.InputFormDialogRecipient;
import au.com.vaadinutils.js.JSCallWithReturnValue;
import au.com.vaadinutils.js.JavaScriptCallback;

/** A start view for navigating to the main view */

public abstract class DashBoardView extends VerticalLayout implements View
{

	private static final long serialVersionUID = 1L;

	Logger logger = LogManager.getLogger();

	private GridStackLayoutNoJQuery dashBoard;

	private ListSelect dashBoardSelector;

	private BeanItemContainer<Tblportallayout> container;

	private SliderPanel dashboardsSlider;

	UI ui = UI.getCurrent();

	private boolean loadJQuery;

	private VerticalLayout toolbarHolder;

	private String style;

	private boolean loading = false;

	Panel dashBoardHolderPanel = new Panel();

	protected DashBoardView(boolean loadJQuery, String style)
	{
		this.loadJQuery = loadJQuery;
		if (style == null)
		{
			this.style = SliderPanelStyles.COLOR_GREEN;
		}
		this.style = style;

	}

	public void closeToolBar()
	{
		dashboardsSlider.collapse();
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		setMargin(new MarginInfo(false, false, false, false));
		setSizeFull();

		final Label preparing = new Label("Preparing your dashboard...");
		preparing.setStyleName(ValoTheme.LABEL_H1);

		addComponent(preparing);

		// defer the load of the dashboard to a separate request, otherwise on a
		// refresh(F5) it will be blank. I think this is due to the DashBoard
		// widget needing to be initialized (client side) first

		new JSCallWithReturnValue("true").callBoolean(new JavaScriptCallback<Boolean>()
		{

			@Override
			public void callback(Boolean value)
			{

				final UI ui = UI.getCurrent();

				new Thread(getLoadRunner(preparing, ui), "Dash Board Delayed Loader").start();

			}

			private Runnable getLoadRunner(final Label preparing, final UI ui)
			{
				return new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException e1)
						{
							logger.error(e1);
						}

						ui.access(new Runnable()
						{

							@Override
							public void run()
							{
								try (AutoCloseable closer = EntityManagerProvider
										.setThreadLocalEntityManagerTryWithResources())
								{
									removeComponent(preparing);
									postLoad();
								}
								catch (Exception e)
								{
									logger.error(e, e);
								}
							}
						});

					}
				};
			}
		});

	}

	void postLoad()
	{
		VerticalLayout sliderHolder = new VerticalLayout();
		sliderHolder.setWidth("80%");
		sliderHolder.setHeight("40");
		addComponent(sliderHolder);
		setComponentAlignment(sliderHolder, Alignment.TOP_CENTER);

		dashboardsSlider = new SliderPanelBuilder(dashboardPanels()).expanded(false).mode(SliderMode.TOP)
				.tabPosition(SliderTabPosition.MIDDLE).style(style).caption("Dashboards").animationDuration(400)
				.tabSize(30).autoCollapseSlider(true).build();
		sliderHolder.addComponent(dashboardsSlider);

		Tblportallayout portalLayout = findDefaultPortal();
		createDashboard(portalLayout);
		dashBoardSelector.select(portalLayout);
		dashBoardHolderPanel.setSizeFull();

		addComponent(dashBoardHolderPanel);
		setExpandRatio(dashBoardHolderPanel, 1);

	}

	void createDashboard(Tblportallayout portalLayout)
	{
		if (portalLayout == null)
		{
			portalLayout = new Tblportallayout();
			portalLayout.setName("Dashboard " + (getNumberOfPortals() + 1));
			portalLayout.setAccount(getAccountId());

			EntityManagerProvider.persist(portalLayout);
			container.addBean(portalLayout);

		}

		if (loadJQuery)
		{
			dashBoard = new DashBoard();
		}
		else
		{
			dashBoard = new DashBoardNoJQuery();
		}

		// this wrapper is necessary so the portals in the dashboard resize
		// correctly
		VerticalLayout wrapper = new VerticalLayout();
		wrapper.setSizeFull();
		wrapper.addComponent(dashBoard);
		dashBoard.setSizeFull();
		dashBoardHolderPanel.setContent(wrapper);

		AbstractLayout dashboardToolBar = createToolBar(new DashBoardController(dashBoard), portalLayout.getGuid());
		toolbarHolder.removeAllComponents();
		toolbarHolder.addComponent(dashboardToolBar);

		loading = true;
		dashBoardSelector.setValue(portalLayout);
		loadDashboard(portalLayout, new DashBoardController(dashBoard));
		loading = false;

		dashboardsSlider.setCaption(portalLayout.getName());

	}

	public abstract AbstractLayout createToolBar(DashBoardController dashBoard2, String guid);

	public abstract Long getAccountId();

	private Component dashboardPanels()
	{
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		Component dashboardManagement = dashboardManagement();
		layout.addComponent(dashboardManagement);
		layout.setComponentAlignment(dashboardManagement, Alignment.TOP_CENTER);

		toolbarHolder = new VerticalLayout();

		layout.addComponent(toolbarHolder);
		layout.setComponentAlignment(toolbarHolder, Alignment.TOP_CENTER);

		return layout;
	}

	private Component dashboardManagement()
	{
		VerticalLayout layout = new VerticalLayout();

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");

		Button newDashboard = new Button(FontAwesome.PLUS);
		newDashboard.setDescription("New Dashboard");
		newDashboard.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		newDashboard.addStyleName(ValoTheme.BUTTON_FRIENDLY);

		Button rename = createRenameButton();

		Button copy = new Button(FontAwesome.COPY);
		copy.setDescription("Copy");
		copy.setStyleName(ValoTheme.BUTTON_ICON_ONLY);

		Button share = new Button(FontAwesome.SHARE);
		share.setDescription("Share");
		share.setStyleName(ValoTheme.BUTTON_ICON_ONLY);

		Button defaultButton = new Button(FontAwesome.STAR);
		defaultButton.setDescription("Default");
		defaultButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		newDashboard.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = -609738416141590613L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				createDashboard(null);

			}
		});

		buttonLayout.addComponent(newDashboard);
		buttonLayout.addComponent(rename);
		buttonLayout.addComponent(createMakeDefaultButton());
		// buttonLayout.addComponent(copy);
		// buttonLayout.addComponent(share);

		createDashboardSelector();

		buttonLayout.addComponent(createDeleteButton());

		TabSheet selectorHolder = new TabSheet();
		selectorHolder.addTab(layout, "Dashboards");

		layout.addComponent(dashBoardSelector);
		layout.setExpandRatio(dashBoardSelector, 1);

		layout.addComponent(buttonLayout);

		// layout.setSizeFull();
		return selectorHolder;
	}

	private Button createRenameButton()
	{
		Button rename = new Button(FontAwesome.EDIT);
		rename.setDescription("Rename Dashboard");
		rename.setStyleName(ValoTheme.BUTTON_ICON_ONLY);

		rename.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1058348590862935257L;

			@Override
			public void buttonClick(ClickEvent event)
			{

				Tblportallayout portalLayout = (Tblportallayout) dashBoardSelector.getValue();
				final TextField primaryFocusField = new TextField("New Dashboard Name");
				AbstractLayout form = new FormLayout();
				form.setSizeFull();
				form.addComponent(primaryFocusField);

				InputFormDialogRecipient recipient = new InputFormDialogRecipient()
				{

					@Override
					public boolean onOK()
					{
						Tblportallayout portalLayout = (Tblportallayout) dashBoardSelector.getValue();
						container.removeItem(portalLayout);
						portalLayout = JpaBaseDao.getGenericDao(Tblportallayout.class).findById(portalLayout.getId());
						portalLayout.setName(primaryFocusField.getValue());
						container.addBean(portalLayout);

						dashBoardSelector.select(portalLayout);
						dashboardsSlider.setCaption("Dashboards: " + portalLayout.getName());

						return true;
					}

					@Override
					public boolean onCancel()
					{
						return true;
					}
				};
				InputFormDialog dialog = new InputFormDialog(UI.getCurrent(),
						"Rename " + portalLayout.getName() + " to:", primaryFocusField, form, recipient);

			}
		});

		return rename;
	}

	private Button createMakeDefaultButton()
	{

		Button rename = new Button(FontAwesome.STAR);

		rename.setDescription("Make Default Dashboard");
		rename.setStyleName(ValoTheme.BUTTON_ICON_ONLY);

		rename.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1058348590862935257L;

			@Override
			public void buttonClick(ClickEvent event)
			{

				// set all portals to not default
				JpaDslBuilder<Tblportallayout> q = JpaBaseDao.getGenericDao(Tblportallayout.class).select();
				List<Tblportallayout> portals = q.where(q.eq(Tblportallayout_.account, getAccountId())).getResultList();
				for (Tblportallayout portal : portals)
				{
					portal.setDefault_(false);
				}

				// set selected portal to default
				Tblportallayout portalLayout = (Tblportallayout) dashBoardSelector.getValue();
				portalLayout = JpaBaseDao.getGenericDao(Tblportallayout.class).findById(portalLayout.getId());
				portalLayout.setDefault_(true);

				// load list and select and display portal
				loadDashboardList();
				dashBoardSelector.select(portalLayout);
				dashboardsSlider.setCaption("Dashboards: " + portalLayout.getName());

			}
		});

		return rename;
	}

	private Button createDeleteButton()
	{
		Button delete = new Button(FontAwesome.TRASH);
		delete.setDescription("Delete Dashboard");
		delete.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		delete.addStyleName(ValoTheme.BUTTON_DANGER);

		delete.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 4136469280694751393L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				Tblportallayout portalLayout = (Tblportallayout) dashBoardSelector.getValue();
				dashBoardSelector.removeItem(portalLayout);
				portalLayout = JpaBaseDao.getGenericDao(Tblportallayout.class).findById(portalLayout.getId());
				EntityManagerProvider.remove(portalLayout);

				final Iterator<?> iterator = dashBoardSelector.getContainerDataSource().getItemIds().iterator();
				if (iterator.hasNext())
				{
					Tblportallayout next = (Tblportallayout) iterator.next();
					dashBoardSelector.select(next);
				}
				else
				{
					createDashboard(null);
				}

			}
		});
		return delete;
	}

	private void createDashboardSelector()
	{
		dashBoardSelector = new ListSelect();
		dashBoardSelector.setHeight("100%");
		dashBoardSelector.setWidth("300");

		// dashBoardSelector.setItemCaptionPropertyId(Tblportallayout_.name.getName());
		// dashBoardSelector.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		container = new BeanItemContainer<>(Tblportallayout.class);
		loadDashboardList();
		dashBoardSelector.setContainerDataSource(container);
		dashBoardSelector.setNullSelectionAllowed(false);

		dashBoardSelector.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 2850017605363067882L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{

				if (!loading)
				{
					Tblportallayout portalLayout = (Tblportallayout) event.getProperty().getValue();
					if (portalLayout != null)
					{
						portalLayout = JpaBaseDao.getGenericDao(Tblportallayout.class).findByEntityId(portalLayout);
						createDashboard(portalLayout);
						closeToolBar();
					}
				}
			}
		});

	}

	private void loadDashboardList()
	{
		Long account = getAccountId();
		JpaDslBuilder<Tblportallayout> q = JpaBaseDao.getGenericDao(Tblportallayout.class).select();
		List<Tblportallayout> layouts = q.where(q.eq(Tblportallayout_.account, account))
				.orderBy(Tblportallayout_.default_, false).orderBy(Tblportallayout_.name, true).getResultList();
		container.removeAllItems();
		container.addAll(layouts);
	}

	private void loadDashboard(Tblportallayout portalLayout, DashBoardController dashBoard2)

	{
		logger.info("Load dash board");
		for (Tblportal portal : portalLayout.getPortals())
		{
			try
			{
				PortalAdderIfc panel = getEnumFromType(portal.getType()).instancePortalAdder(portalLayout.getGuid());

				panel.addPortal(dashBoard2, portal);
			}
			catch (Throwable e)
			{
				logger.error(e, e);
			}
		}

	}

	protected abstract PortalEnumIfc getEnumFromType(String type);

	private Tblportallayout findDefaultPortal()
	{
		Long account = getAccountId();
		JpaDslBuilder<Tblportallayout> q = JpaBaseDao.getGenericDao(Tblportallayout.class).select();
		List<Tblportallayout> layouts = q.where(q.eq(Tblportallayout_.account, account))
				.orderBy(Tblportallayout_.default_, false).orderBy(Tblportallayout_.name, true).getResultList();

		if (layouts.size() == 0)
		{
			return null;
		}
		return layouts.get(0);
	}

	private long getNumberOfPortals()
	{
		Long account = getAccountId();
		JpaDslBuilder<Tblportallayout> q = JpaBaseDao.getGenericDao(Tblportallayout.class).select();
		return q.where(q.eq(Tblportallayout_.account, account)).count();

	}

}
