package au.com.vaadinutils.jasper;

import org.joda.time.DateTime;

public interface ReportFilterDateFieldBuilder
{

	ReportFilterDateFieldBuilder addDateField(String label, String paramName);

	void setDate(DateTime date);

}
