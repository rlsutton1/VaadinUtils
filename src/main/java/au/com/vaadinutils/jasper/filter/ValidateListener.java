package au.com.vaadinutils.jasper.filter;

import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractComponent;

public interface ValidateListener
{


	void setComponentError(ErrorMessage componentError);

	
}
