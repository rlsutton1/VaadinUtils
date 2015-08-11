package au.com.vaadinutils.jasper.ui;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

/**
 * This class is used to allow a 'Popup' drill down report to inherit its
 * parents report properties with the ability to over-ride the :
 * 
 * ReportTitle, ReportFileName and ReportParams.
 * 
 * @author bsutton
 *
 */
public class ChildJasperReportProperties implements JasperReportProperties
{

	private JasperReportProperties parentReportProperties;
	private String reportTitle;
	private String reportFilename;
	private List<ReportParameter<?>> childParams;

	public ChildJasperReportProperties(JasperReportProperties parentReportProperties, String childTitle,
			String childReportFileName, List<ReportParameter<?>> childParams)
	{
		this.parentReportProperties = parentReportProperties;
		this.reportTitle = childTitle;
		this.reportFilename = childReportFileName;
		this.childParams = childParams;
	}

	@Override
	public String getReportTitle()
	{
		return this.reportTitle;
	}

	@Override
	public String getReportFileName()
	{
		return this.reportFilename;
	}
	
	@Override
	public Map<String, Object> getCustomReportParameterMap()
	{
		return parentReportProperties.getCustomReportParameterMap();
	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder()
	{
		ReportFilterUIBuilder builder = new ReportFilterUIBuilder();

		for (ReportParameter<?> filter : childParams)
		{
			builder.addField(filter);
		}

		return builder;
	}

	@Override
	public File getReportFolder()
	{
		return parentReportProperties.getReportFolder();
	}

	@Override
	public String getHeaderFooterTemplateName()
	{
		return parentReportProperties.getHeaderFooterTemplateName();
	}

	@Override
	public String getUsername()
	{
		return this.parentReportProperties.getUsername();
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params,String reportFilename, CleanupCallback cleanupCallback)
			throws Exception
	{
		return parentReportProperties.prepareData(params,reportFilename, cleanupCallback);
	}

	@Override
	public CleanupCallback getCleanupCallback()
	{
		return this.parentReportProperties.getCleanupCallback();
	}

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{
		this.parentReportProperties.prepareForOutputFormat(outputFormat);
	}

	@Override
	public void closeDBConnection()
	{
		this.parentReportProperties.closeDBConnection();

	}

	@Override
	public void initDBConnection()
	{
		this.parentReportProperties.initDBConnection();

	}

	@Override
	public Connection getConnection()
	{
		return this.parentReportProperties.getConnection();
	}

	@Override
	public OutputFormat getDefaultFormat()
	{
		return this.parentReportProperties.getDefaultFormat();
	}

	@Override
	public String generateDynamicHeaderImage(int pageWidth, String reportTitle)
	{
		return this.parentReportProperties.generateDynamicHeaderImage(pageWidth, reportTitle);
	}

	@Override
	public boolean isDevMode()
	{
		return this.parentReportProperties.isDevMode();
	}

	@Override
	public Class<? extends JasperReportProperties> getReportClass()
	{
		return this.parentReportProperties.getReportClass();
	}

	@Override
	public String getUserEmailAddress()
	{

		return this.parentReportProperties.getUserEmailAddress();
	}

	@Override
	public Enum<?> getReportIdentifier()
	{
		return this.parentReportProperties.getReportIdentifier();
	}

}
