package au.com.vaadinutils.jasper.ui;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

/**
 * Provides an interface between Jasper Reports and you application specific settings
 * and the settings of a particular report.
 * 
 * Normally you would implement a base abstract class that implemented each of the method with the 
 * exception of:
 * 
 * public abstract String getReportTitle();
 * 
 * public abstract String getReportFileName();
 * 
 * public abstract ReportFilterUIBuilder getFilterBuilder();
 * 
 * Each of the above methods are then implemented in a concrete class for each report.
 * 
 * @author bsutton
 *
 */
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
	 * file will be written (if the jrxml needs to be compiled).
	 * @return
	 */
	public abstract File getReportFolder();

	/**
	 * The library supports the concept of a standardized header and footer which is dynamically 
	 * added to each report. This provides a nice means of ensuring that every report has a consistent
	 * header and footer.
	 * The template is just a standard jasper report which contains just three bands:
	 * Page Header
	 * Page Footer
	 * No Details section.
	 * 
	 * When creating a report you should leave each of the above bands empty so they can be dynamically
	 * rendered.
	 * 
	 * The band may contain parameters such as the report name.
	 * 
	 * @return the name of the template jasper file (something like
	 *         "HeaderFooter") or null if your not using a template
	 */
	public abstract String getHeaderFooterTemplateName();

	/**
	 * This should return the current logged in users name.
	 * It is displayed in the list of queued reports so that other users can know who is
	 * holding the queue up.
	 * 
	 * @return
	 */
	public abstract String getUsername();

	public abstract ReportFilterUIBuilder getFilterBuilder();

	/**
	 * ***MUST BE THREAD SAFE***
	 * <p>
	 * Some more complex reports may need some preprocessing of data before the 
	 * report is run. A classic example is using java code to create a temporary table
	 * which is then passed to the report as its primary data source (via a parameter).
	 * The prepareData method is called before the jasper report is rendered.
	 * 
	 * Create any temporary data that may be needed for the jasper report and
	 * return any extra params, typically the temporary table
	 * <p>
	 * Because report parameters live through multiple runs of a report and the
	 * agent may cancel a report, which leaves the report still running in a background thread.
	 * 
	 * prepareData must be thread safe.
	 * <p>
	 * if cleanup is required it should be done via the CleanupCallback
	 * Object which will be invoked after the report is finished rendering.
	 * 
	 * @param params the collection of parameters that will be sent to the report.
	 * @param cleanupCallback - a interface to an object that will be called once the report has completed.
	 * 
	 * @return The list or parameters that are to be passed to the report. This is normally 'params' with any 
	 * additional parameters added to the collection.
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