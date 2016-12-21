package au.com.vaadinutils.dashboard;

import java.util.Map;

import org.vaadin.alump.gridstack.GridStackCoordinates;

import com.vaadin.ui.HorizontalLayout;

public interface PortalConfigDelgate
{

	String getTitle();

	void setValue(Tblportal portal, String key, int value);

	void setValue(Tblportal portal, String key, String value);

	String getValueString(Tblportal portal, String key);

	void savePosition(Tblportal portal, GridStackCoordinates event);

	Integer getValueInt(Tblportal portal, String key, Integer defaultValue);

	Map<String, Integer> getValuesLikeInt(Tblportal portal, String string);

	void deleteValuesLike(Tblportal portal, String baseKey);

	void addCustomHeaderButtons(HorizontalLayout controlLayout);

	double getValueDouble(Tblportal tblportal, String string);

	void addResizeListener(PortalResizeListener resizeListener);

}
