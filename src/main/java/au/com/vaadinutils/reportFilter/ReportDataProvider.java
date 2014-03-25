package au.com.vaadinutils.reportFilter;

import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.ReportFilterUIBuilder;

public interface ReportDataProvider
{

	ReportFilterUIBuilder getFilterBuilder(JasperManager manager);

	/**
	 * return an extra param, typically the temporary table
	 * 
	 * @param params
	 * @param reportFileName 
	 * @return
	 * @throws Exception
	 */
	List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFileName) throws Exception;

	void cleanup();

	/**
	 * opportunity to set custom formatters for fields based on output format. eg: a machine readable time
	 * output format for CSV.
	 * 
	 * @param outputFormat
	 */
	void prepareForOutputFormat(OutputFormat outputFormat);

}
