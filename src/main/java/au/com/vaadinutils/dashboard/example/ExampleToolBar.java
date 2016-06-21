package au.com.vaadinutils.dashboard.example;

import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.dashboard.DashBoard;

public class ExampleToolBar extends VerticalLayout
{
	// Logger logger = LogManager.getLogger();

	private static final long serialVersionUID = 1L;

	ExampleToolBar(DashBoard dashBoard, String portalLayoutGuid)
	{
		setSizeFull();
		setWidth("450");
		setSpacing(true);
		setMargin(true);

		addComponent(ExampleDashboardEnum.EXAMPLE.instancePortalAdder(portalLayoutGuid).getVaadinAddLayout(dashBoard));
	}
}
