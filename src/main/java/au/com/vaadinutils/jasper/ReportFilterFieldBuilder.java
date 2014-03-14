package au.com.vaadinutils.jasper;

import au.com.vaadinutils.reportFilter.ReportParameter;

import com.vaadin.ui.AbstractLayout;

public interface ReportFilterFieldBuilder
{

	/**
	 * Adds a field for a jasper parameter.
	 * 
	 * @param label
	 *            the Label to display when this parameter is presented to the
	 *            user.
	 * @param paramName
	 *            the name of the jasper parameter.
	 * @return
	 */
	ReportFilterFieldBuilder addField(ReportParameter<?> param);
	ReportFilterFieldBuilder addTextField(String label, String paramName);
	ReportFilterDateFieldBuilder addDateField(String label, String paramName) ;
	<T extends Enum<?>> ReportFilterFieldBuilder addEnumField(String label, String paramName, Class<T> class1, T day);
	
	AbstractLayout buildLayout();

}
