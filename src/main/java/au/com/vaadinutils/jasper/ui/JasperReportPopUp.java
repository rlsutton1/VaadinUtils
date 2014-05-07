package au.com.vaadinutils.jasper.ui;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public class JasperReportPopUp extends Window implements JasperReportProperties
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	// static private final transient Logger logger = LogManager.getLogger();

	final JasperReportLayout report;
	private List<ReportParameter<?>> filters;
	private JasperReportProperties reportPropertiesTemplate;
	private String title;
	private String fileName;

	public JasperReportPopUp(String subTitle, String subReportFileName, JasperReportProperties reportPropertiesTemplate, List<ReportParameter<?>> filters)
	{
		this.filters = filters;
		
		title = subTitle;
		fileName = subReportFileName;

		// replace the data provider in the report properties so this class can
		// replace the filters
		this.reportPropertiesTemplate = reportPropertiesTemplate;
		report = new JasperReportLayout(this);

		init();

	}

	public JasperReportPopUp(JasperReportProperties reportPropertiesTemplate, List<ReportParameter<?>> filters)
	{
		this.filters = filters;
		
		title = reportPropertiesTemplate.getReportTitle();
		fileName = reportPropertiesTemplate.getReportFileName();

		// replace the data provider in the report properties so this class can
		// replace the filters
		this.reportPropertiesTemplate = reportPropertiesTemplate;
		report = new JasperReportLayout(this);

		init();
	}

	private void init()
	{
		this.setWidth("90%");
		this.setHeight("80%");
		report.initScreen(new DrillDownReportSplitPanel());
		this.setContent(report);
		setModal(true);
		// center();
		UI.getCurrent().addWindow(this);

	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder()
	{
		ReportFilterUIBuilder builder = new ReportFilterUIBuilder();

		for (ReportParameter<?> filter : filters)
		{
			builder.addField(filter);
		}

		return builder;
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFileName, CleanupCallback cleanupCallback)
			throws Exception
	{
		return reportPropertiesTemplate.prepareData(params, reportFileName, cleanupCallback);
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
	public String getReportFileName()
	{
		return fileName;
	}

	@Override
	public String getReportTitle()
	{
		return title;
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

}
