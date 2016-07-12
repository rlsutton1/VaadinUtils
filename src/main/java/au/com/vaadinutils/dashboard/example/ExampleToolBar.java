package au.com.vaadinutils.dashboard.example;

import org.vaadin.alump.gridstack.GridStackLayoutNoJQuery;

import com.vaadin.ui.VerticalLayout;

public class ExampleToolBar extends VerticalLayout
{
	// Logger logger = LogManager.getLogger();

	private static final long serialVersionUID = 1L;

	ExampleToolBar(GridStackLayoutNoJQuery dashBoard, String portalLayoutGuid)
	{
		setSizeFull();
		setWidth("450");
		setSpacing(true);
		setMargin(true);

		addComponent(ExampleDashboardEnum.EXAMPLE.instancePortalAdder(portalLayoutGuid).getVaadinAddLayout(dashBoard));
	}
}
