package au.com.vaadinutils.jasper.ui;

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
public class JasperReportPopUp extends Window implements JasperReportDataProvider
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	// static private final transient Logger logger = LogManager.getLogger();

	final JasperReportLayout report;
	private List<ReportParameter<?>> filters;
	private JasperReportDataProvider dataProvider;

	public JasperReportPopUp(JasperReportProperties subReportProperties, List<ReportParameter<?>> filters)
	{
		this.filters = filters;
		this.dataProvider = subReportProperties.getDataProvider();

		// replace the data provider in the report properties so this class can
		// replace the filters
		JasperReportProperties properties = new JasperReportProperties(subReportProperties.getReportTitle(),
				subReportProperties.getReportFileName(), this, subReportProperties.getEm(),
				subReportProperties.getSettings());
		report = new JasperReportLayout(properties);

		init();

	}

	private void init()
	{
		this.setWidth("90%");
		this.setHeight("80%");
		report.initScreen();
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
		return dataProvider.prepareData(params, reportFileName, cleanupCallback);
	}

	

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{
		dataProvider.prepareForOutputFormat(outputFormat);

	}

	@Override
	public void closeDBConnection()
	{
		dataProvider.closeDBConnection();

	}

	@Override
	public void initDBConnection()
	{
		dataProvider.initDBConnection();

	}

	@Override
	public OutputFormat getDefaultFormat()
	{
		return dataProvider.getDefaultFormat();
	}

	@Override
	public CleanupCallback getCleanupCallback()
	{
		return dataProvider.getCleanupCallback();
	}

}
