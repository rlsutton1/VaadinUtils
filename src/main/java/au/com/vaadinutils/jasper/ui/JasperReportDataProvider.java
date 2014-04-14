package au.com.vaadinutils.jasper.ui;

import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

public interface JasperReportDataProvider
{

	ReportFilterUIBuilder getFilterBuilder();

	/**
	 * ***MUST BE THREAD SAFE***<p>
	 * 
	 * because report parameters live through multiple runs of a report and the
	 * agent may cancel a report, which leaves the report still running.
	 * prepareData must be thread safe.
	 * <p>
	 * create any temporary data that may be needed for the jasper report and
	 * return any extra params, typically the temporary table
	 * <p>
	 * if cleanup is required it should be associated with the CleanupCallback
	 * Object which will be invoked after the report is finished rendering.
	 * 
	 * @param params
	 * @param reportFileName
	 * @param cleanupCallback
	 *            TODO
	 * @return
	 * @throws Exception
	 */
	List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFileName,
			CleanupCallback cleanupCallback) throws Exception;

	/**
	 * because report parameters live through multiple runs of a report and the
	 * agent may cancel a report, which leaves the report still running.<p>
	 * prepareData must be thread safe.<p> So this method allows creation of a
	 * cleanupcallback, that is passed to prepareData. Anything that prepare
	 * data does that needs to be cleaned up should be tracked in the
	 * CleanupCallback Object.
	 * 
	 * @return
	 */
	CleanupCallback getCleanupCallback();

	/**
	 * opportunity to set custom formatters for fields based on output format.
	 * eg: a machine readable time output format for CSV.
	 * 
	 * @param outputFormat
	 */
	void prepareForOutputFormat(OutputFormat outputFormat);

	void closeDBConnection();

	void initDBConnection();

	/**
	 * if there are no parameters the report will automatically be rendered when
	 * the report window first opens. This method controls which formatter will
	 * be used HTML/PDF/CSV
	 * 
	 * @return
	 */
	OutputFormat getDefaultFormat();

	/**
	 * return the name of the generated header image file
	 * @param pageWidth
	 * @param reportTitle TODO
	 * @return
	 */
	String generateDynamicHeaderImage(int pageWidth, String reportTitle);

}
