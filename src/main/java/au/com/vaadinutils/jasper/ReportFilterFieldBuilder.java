package au.com.vaadinutils.jasper;

import com.vaadin.ui.AbstractLayout;

public interface ReportFilterFieldBuilder
{

	/**
	 * Adds a field for a jasper parameter.
	 * 
	 * @param label the Label to display when this parameter is presented to the user.
	 * @param paramName the name of the jasper parameter.
	 * @return
	 */
	ReportFilterFieldBuilder addField(String label, String paramName);

	AbstractLayout buildLayout();

}
