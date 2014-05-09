package au.com.vaadinutils.jasper.ui;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

public interface JasperReportProperties
{

	/**
	 * @return the reportTitle, this will be displayed in the report window and
	 *         on the header of the report
	 */
	public abstract String getReportTitle();

	/**
	 * the name of the jasper report file with or without extension.
	 * 
	 * @return
	 */
	public abstract String getReportFileName();

	/**
	 * the folder where the jasper report (.jrxml) can be found and the .jasper
	 * file will be written.
	 * 
	 * @return
	 */
	public abstract File getReportFolder();

	/**
	 * 
	 * @return the name of the template jasper file (something like
	 *         "HeaderFooter") or null if your not using a template
	 */
	public abstract String getHeaderFooterTemplateName();

	/**
	 * displayed in the list of queued reports
	 * 
	 * @return
	 */
	public abstract String getUsername();

	public abstract ReportFilterUIBuilder getFilterBuilder();

	/**
	 * ***MUST BE THREAD SAFE***
	 * <p>
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
	public abstract List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFileName,
			CleanupCallback cleanupCallback) throws Exception;

	/**
	 * because report parameters live through multiple runs of a report and the
	 * agent may cancel a report, which leaves the report still running.
	 * <p>
	 * prepareData must be thread safe.
	 * <p>
	 * So this method allows creation of a cleanupcallback, that is passed to
	 * prepareData. Anything that prepare data does that needs to be cleaned up
	 * should be tracked in the CleanupCallback Object.
	 * 
	 * @return
	 */
	public abstract CleanupCallback getCleanupCallback();

	/**
	 * opportunity to set custom formatters for fields based on output format.
	 * eg: a machine readable time output format for CSV.
	 * 
	 * @param outputFormat
	 */
	public abstract void prepareForOutputFormat(OutputFormat outputFormat);

	/**
	 * close the database connection that was created when initDBConnection was
	 * called.
	 */
	public abstract void closeDBConnection();

	/**
	 * initialize a database connection, if you use a db connection in
	 * prepareData you should use the one created here.
	 */
	public abstract void initDBConnection();

	/**
	 * after prepare data has been called, the jasper report call this method to
	 * get a connection.
	 * <p>
	 * you should provide the db connection that was created when
	 * initDBConnection was called.
	 * 
	 * @return
	 */
	public abstract Connection getConnection();

	/**
	 * if there are no parameters the report will automatically be rendered when
	 * the report window first opens.
	 * <p>
	 * This method controls which formatter will be used HTML/PDF/CSV
	 * 
	 * @return
	 */
	public abstract OutputFormat getDefaultFormat();

	/**
	 * only called if a template is in use. see "getHeaderFooterTemplateName"
	 * <p>
	 * this is an optional opportunity to generate a header image. if the
	 * template contains an image and the image expression contains "logo.png"
	 * then "logo.png" will be replaced with the filename returned from this
	 * method. This should lead to the generated image being displayed in the
	 * report
	 * 
	 * 
	 * @param pageWidth
	 * @param reportTitle
	 *            TODO
	 * @return
	 */
	public abstract String generateDynamicHeaderImage(int pageWidth, String reportTitle);

	/**
	 * reports will be recompiled on every run if true
	 * 
	 * @return
	 */
	public abstract boolean isDevMode();

	/**
	 * the name of the class that knows how to render the filter builder and
	 * prepare data and provide a jasperreportprovider
	 * 
	 * @return
	 */
	public abstract Class<? extends JasperReportProperties> getReportClass();

	public abstract String getUserEmailAddress();

	/**
	 * Multiple reports can be accessed from a single ReportView via the ReportChooserReportParameter, so it is 
	 * necessary to be able to identify a report as belonging to a ReportView
	 * 
	 * An enum that identifies a report based on the vaadin view that is used to configure it.
	 * this will be used to group the report in the schedules view.
	 * 
	 * @return
	 */
	public abstract Enum<?> getReportIdentifier();

}