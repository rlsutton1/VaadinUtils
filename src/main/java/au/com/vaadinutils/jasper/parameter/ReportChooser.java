package au.com.vaadinutils.jasper.parameter;

import au.com.vaadinutils.jasper.ui.JasperReportDataProvider;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

public interface ReportChooser
{

	JasperReportProperties getReportProperties(JasperReportDataProvider dataProvider);

}
