package au.com.vaadinutils.jasper.filter;

import java.util.List;

import au.com.vaadinutils.jasper.parameter.ReportParameter;

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
	
	List<ExpanderComponent> buildLayout(Boolean hideDateFields);

}
