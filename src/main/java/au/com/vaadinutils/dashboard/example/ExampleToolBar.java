package au.com.vaadinutils.dashboard.example;

import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.dashboard.DashBoardController;
import au.com.vaadinutils.dashboard.DashBoardView;

public class ExampleToolBar extends VerticalLayout
{
	// Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private static final long serialVersionUID = 1L;

	ExampleToolBar(DashBoardController dashBoard, String portalLayoutGuid, DashBoardView view)
	{
		setSizeFull();
		setWidth("450");
		setSpacing(true);
		setMargin(true);

		addComponent(
				ExampleDashboardEnum.EXAMPLE.instancePortalAdder(portalLayoutGuid).getVaadinAddLayout(dashBoard, view));
	}
}
