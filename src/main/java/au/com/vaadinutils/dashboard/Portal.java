package au.com.vaadinutils.dashboard;

import org.vaadin.alump.gridstack.GridStackMoveEvent;

import com.vaadin.ui.Component;

public interface Portal extends Component
{

	void savePosition(GridStackMoveEvent event);

}
