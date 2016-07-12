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
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.editors.InputFormDialog;
import au.com.vaadinutils.editors.InputFormDialogRecipient;

/** A start view for navigating to the main view */

public abstract class DashBoardView extends HorizontalLayout implements View
{

	private static final long serialVersionUID = 1L;

	Logger logger = LogManager.getLogger();

	private GridStackLayoutNoJQuery dashBoard;

	private SliderPanel toolbarSlider;

	private ListSelect dashBoardSelector;

	private BeanItemContainer<Tblportallayout> container;

	private SliderPanel dashboardsSlider;

	UI ui = UI.getCurrent();

	private boolean loadJQuery;

	protected DashBoardView(boolean loadJQuery)
	{
		this.loadJQuery = loadJQuery;
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		setMargin(new MarginInfo(false, false, true, false));
		setSizeFull();

		// defer the load of the dashboard to a separate request, otherwise on a
		// refresh(F5) it will be blank
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try (AutoCloseable closer = EntityManagerProvider.setThreadLocalEntityManagerTryWithResources())
				{
					Thread.sleep(50);
					ui.access(new Runnable()
					{

						@Override
						public void run()
						{
							postLoad();
						}
					});

				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		}).start();

	}

	void postLoad()
	{
		VerticalLayout sliderHolder = new VerticalLayout();
		sliderHolder.setWidth("30");
		sliderHolder.setHeight("100%");
		addComponent(sliderHolder);

		dashboardsSlider = new SliderPanelBuilder(dashboardManagement()).expanded(false).mode(SliderMode.LEFT)
				.tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_GREEN).caption("Dashboards")
				.animationDuration(400).tabSize(30).autoCollapseSlider(true).build();
		sliderHolder.addComponent(dashboardsSlider);

		toolbarSlider = new SliderPanelBuilder(new VerticalLayout()).expanded(false).mode(SliderMode.LEFT)
				.caption("Add Widgets").style(SliderPanelStyles.COLOR_WHITE).animationDuration(400)
				.tabPosition(SliderTabPosition.BEGINNING).autoCollapseSlider(true).tabSize(30).build();
		sliderHolder.addComponent(toolbarSlider);

		Tblportallayout portalLayout = findDefaultPortal();
		createDashboard(portalLayout);

	}

	void createDashboard(Tblportallayout portalLayout)
	{
		if (portalLayout == null)
		{
			portalLayout = new Tblportallayout();
			portalLayout.setName("New Dashboard " + System.currentTimeMillis());
			portalLayout.setAccount(getAccountId());

			EntityManagerProvider.persist(portalLayout);
			container.addBean(portalLayout);

		}

		if (dashBoard != null)
		{
			removeComponent(dashBoard);
		}
		if (loadJQuery)
		{
			dashBoard = new DashBoard();
		}
		else
		{
			dashBoard = new DashBoardNoJQuery();
		}

		addComponent(dashBoard);
		setExpandRatio(dashBoard, 1);

		AbstractLayout dashboardToolBar = createToolBar(dashBoard, portalLayout.getGuid());

		toolbarSlider.setContent(dashboardToolBar);
		loadDashboard(portalLayout, dashBoard);
		dashBoardSelector.setValue(portalLayout);
		dashboardsSlider.setCaption("Dashboards: " + portalLayout.getName());

	}

	public abstract AbstractLayout createToolBar(GridStackLayoutNoJQuery dashBoard2, String guid);

	public abstract Long getAccountId();

	private Component dashboardManagement()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setHeight("100%");
		layout.setMargin(new MarginInfo(true, true, true, true));

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");

		Button newDashboard = new Button(FontAwesome.DASHBOARD);
		newDashboard.setDescription("New");
		newDashboard.setStyleName(ValoTheme.BUTTON_ICON_ONLY);

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
		// buttonLayout.addComponent(copy);
		// buttonLayout.addComponent(share);

		createDashboardSelector();

		buttonLayout.addComponent(createDeleteButton());

		layout.addComponent(dashBoardSelector);
		layout.setExpandRatio(dashBoardSelector, 1);

		layout.addComponent(buttonLayout);

		// layout.setSizeFull();
		return layout;
	}

	private Button createRenameButton()
	{
		Button rename = new Button(FontAwesome.GEAR);
		rename.setDescription("Rename");
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

	private Button createDeleteButton()
	{
		Button delete = new Button(FontAwesome.TRASH);
		delete.setDescription("Delete");
		delete.setStyleName(ValoTheme.BUTTON_ICON_ONLY);

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
		dashBoardSelector = new ListSelect("Dashboards");
		dashBoardSelector.setHeight("100%");
		dashBoardSelector.setWidth("200");

		dashBoardSelector.setItemCaptionPropertyId(Tblportallayout_.name.getName());
		dashBoardSelector.setItemCaptionMode(ItemCaptionMode.PROPERTY);

		Long account = getAccountId();
		List<Tblportallayout> layouts = JpaBaseDao.getGenericDao(Tblportallayout.class)
				.findAllByAttribute(Tblportallayout_.account, account, null);
		container = new BeanItemContainer<>(Tblportallayout.class);
		container.addAll(layouts);
		dashBoardSelector.setContainerDataSource(container);
		dashBoardSelector.setNullSelectionAllowed(false);

		dashBoardSelector.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 2850017605363067882L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{

				Tblportallayout portalLayout = (Tblportallayout) event.getProperty().getValue();
				if (portalLayout != null)
				{
					portalLayout = JpaBaseDao.getGenericDao(Tblportallayout.class).findByEntityId(portalLayout);
					createDashboard(portalLayout);
				}
			}
		});

	}

	private void loadDashboard(Tblportallayout portalLayout, GridStackLayoutNoJQuery dashBoard2)

	{
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
		Tblportallayout portalLayout = JpaBaseDao.getGenericDao(Tblportallayout.class)
				.findOneByAttribute(Tblportallayout_.account, account);

		return portalLayout;
	}

}
