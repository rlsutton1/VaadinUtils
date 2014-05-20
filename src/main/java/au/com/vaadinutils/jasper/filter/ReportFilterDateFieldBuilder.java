package au.com.vaadinutils.jasper.filter;

import java.util.List;

import org.joda.time.DateTime;

public interface ReportFilterDateFieldBuilder extends ReportFilterFieldBuilder
{

	ReportFilterDateFieldBuilder setDate(DateTime date);

	ReportFilterDateFieldBuilder setDateRange(DateTime startDate, DateTime endDate);

	List<ExpanderComponent> buildLayout(Boolean hideDateFields);

}
