package au.com.vaadinutils.jasper.ui;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

public class JasperReportPropertiesAlternateFile implements JasperReportProperties
{

	private String title;
	private String reportFileName;
	private JasperReportProperties reportPropertiesTemplate;

	public JasperReportPropertiesAlternateFile(String title, String reportFileName, JasperReportProperties properties)
	{
		this.title = title;
		this.reportFileName = reportFileName;
		this.reportPropertiesTemplate = properties;
	}

	@Override
	public String getReportFileName()
	{

		return reportFileName;
	}

	@Override
	public String getReportTitle()
	{

		return title;
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, CleanupCallback cleanupCallback)
			throws Exception
	{
		return reportPropertiesTemplate.prepareData(params, cleanupCallback);
	}

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{
		reportPropertiesTemplate.prepareForOutputFormat(outputFormat);

	}

	@Override
	public void closeDBConnection()
	{
		reportPropertiesTemplate.closeDBConnection();

	}

	@Override
	public void initDBConnection()
	{
		reportPropertiesTemplate.initDBConnection();

	}

	@Override
	public OutputFormat getDefaultFormat()
	{
		return reportPropertiesTemplate.getDefaultFormat();
	}

	@Override
	public CleanupCallback getCleanupCallback()
	{
		return reportPropertiesTemplate.getCleanupCallback();
	}

	@Override
	public String generateDynamicHeaderImage(int pageWidth, String reportTitle)
	{
		return reportPropertiesTemplate.generateDynamicHeaderImage(pageWidth, reportTitle);
	}

	@Override
	public boolean isDevMode()
	{
		return reportPropertiesTemplate.isDevMode();
	}

	@Override
	public String getHeaderFooterTemplateName()
	{
		return reportPropertiesTemplate.getHeaderFooterTemplateName();
	}

	@Override
	public String getUsername()
	{
		return reportPropertiesTemplate.getUsername();
	}

	@Override
	public Connection getConnection()
	{
		return reportPropertiesTemplate.getConnection();
	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder()
	{
		return reportPropertiesTemplate.getFilterBuilder();
	}

	@Override
	public File getReportFolder()
	{
		return reportPropertiesTemplate.getReportFolder();
	}

	@Override
	public Class<? extends JasperReportProperties> getReportClass()
	{
		return reportPropertiesTemplate.getReportClass();
	}

	@Override
	public String getUserEmailAddress()
	{
		return reportPropertiesTemplate.getUserEmailAddress();
	}

	@Override
	public Enum<?> getReportIdentifier()
	{
		return reportPropertiesTemplate.getReportIdentifier();
	}
}
