package au.com.vaadinutils.dashboard.example;

import com.vaadin.ui.AbstractLayout;

import au.com.vaadinutils.dashboard.CopyDashboardCallback;
import au.com.vaadinutils.dashboard.DashBoardController;
import au.com.vaadinutils.dashboard.DashBoardView;
import au.com.vaadinutils.dashboard.PortalEnumIfc;

public class ExampleDashboard extends DashBoardView
{

	protected ExampleDashboard(boolean loadJQuery)
	{
		// load the JQuery...
		super(true, null);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public AbstractLayout createToolBar(DashBoardController dashBoard, String guid)
	{
		return new ExampleToolBar(dashBoard, guid, this);
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
	// Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@Override
	protected void getAccountIdToShareDashboardWith(CopyDashboardCallback callback)
	{
		// TODO Auto-generated method stub

	}
}
