package au.com.vaadinutils.jasper.parameter;

import au.com.vaadinutils.jasper.ui.JasperReportDataProvider;
import au.com.vaadinutils.jasper.ui.ReportProperties;

public interface ReportChooser
{

	ReportProperties getReportProperties(JasperReportDataProvider dataProvider);

}
