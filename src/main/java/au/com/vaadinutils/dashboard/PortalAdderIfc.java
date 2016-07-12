package au.com.vaadinutils.dashboard;

import org.vaadin.alump.gridstack.GridStackLayoutNoJQuery;

import com.vaadin.ui.Component;

public interface PortalAdderIfc
{

	String getTitle();

	Component getVaadinAddLayout(GridStackLayoutNoJQuery dashBoard);

	void addPortal(GridStackLayoutNoJQuery dashBoard2, Tblportal portal);

}
