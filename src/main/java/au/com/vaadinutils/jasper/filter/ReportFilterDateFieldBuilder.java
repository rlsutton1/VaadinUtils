package au.com.vaadinutils.jasper.filter;

import org.joda.time.DateTime;

public interface ReportFilterDateFieldBuilder extends ReportFilterFieldBuilder
{

	ReportFilterDateFieldBuilder setDate(DateTime date);

}
