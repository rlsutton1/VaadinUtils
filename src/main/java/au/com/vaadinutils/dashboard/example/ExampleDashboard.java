package au.com.vaadinutils.dashboard.example;

import com.vaadin.ui.AbstractLayout;

import au.com.vaadinutils.dashboard.DashBoard;
import au.com.vaadinutils.dashboard.DashBoardView;
import au.com.vaadinutils.dashboard.PortalEnumIfc;

public class ExampleDashboard extends DashBoardView
{

	private static final long serialVersionUID = 1L;

	@Override
	public AbstractLayout createToolBar(DashBoard dashBoard, String guid)
	{
		return new ExampleToolBar(dashBoard, guid);
	}

	@Override
	public Long getAccountId()
	{
		return 1L;
	}

	@Override
	protected PortalEnumIfc getEnumFromType(String type)
	{
		return ExampleDashboardEnum.valueOf(type);
	}
	// Logger logger = LogManager.getLogger();
}
