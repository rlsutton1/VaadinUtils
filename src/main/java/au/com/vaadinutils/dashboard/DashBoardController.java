package au.com.vaadinutils.dashboard;

import org.vaadin.alump.gridstack.GridStackLayoutNoJQuery;
import org.vaadin.alump.gridstack.GridStackMoveEvent.GridStackMoveListener;

import com.vaadin.ui.Component;

public class DashBoardController
{

	boolean canUpdate = true;
	GridStackLayoutNoJQuery actualDashBoard;

	DashBoardController(GridStackLayoutNoJQuery dashBoard)
	{
		actualDashBoard = dashBoard;
	}

	public boolean canUpdate()
	{
		return canUpdate;
	}

	public void setCanUpdate(boolean b)
	{
		canUpdate = b;
	}

	public void addComponent(Component component)
	{
		actualDashBoard.addComponent(component);

	}

	public void moveAndResizeComponent(Portal component, Integer x, Integer y, Integer width, Integer height)
	{
		actualDashBoard.moveAndResizeComponent(component, x, y, width, height);

	}

	public void removeGridStackMoveListener(GridStackMoveListener moveListener)
	{
		actualDashBoard.removeGridStackMoveListener(moveListener);

	}

	public void addGridStackMoveListener(GridStackMoveListener moveListener)
	{
		actualDashBoard.addGridStackMoveListener(moveListener);

	}

	public void removeComponent(BasePortal basePortal)
	{
		actualDashBoard.removeComponent(basePortal);

	}

}
