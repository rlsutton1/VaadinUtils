package au.com.vaadinutils.dashboard;

import com.vaadin.ui.Component;

public interface PortalAdderIfc
{

	String getTitle();

	Component getVaadinAddLayout(DashBoard dashBoard);

	void addPortal(DashBoard dashBoard, Tblportal portal);

}
