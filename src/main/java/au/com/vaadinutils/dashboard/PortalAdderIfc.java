package au.com.vaadinutils.dashboard;

import com.vaadin.ui.Component;

public interface PortalAdderIfc
{

	String getTitle();

	Component getVaadinAddLayout(DashBoardController dashBoard, DashBoardView view);

	void addPortal(DashBoardController dashBoard2, Tblportal portal);

}
