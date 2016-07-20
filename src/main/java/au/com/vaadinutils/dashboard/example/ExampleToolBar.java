package au.com.vaadinutils.dashboard.example;

import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.dashboard.DashBoardController;

public class ExampleToolBar extends VerticalLayout
{
	// Logger logger = LogManager.getLogger();

	private static final long serialVersionUID = 1L;

	ExampleToolBar(DashBoardController dashBoard, String portalLayoutGuid)
	{
		setSizeFull();
		setWidth("450");
		setSpacing(true);
		setMargin(true);

		addComponent(ExampleDashboardEnum.EXAMPLE.instancePortalAdder(portalLayoutGuid).getVaadinAddLayout(dashBoard));
	}
}
