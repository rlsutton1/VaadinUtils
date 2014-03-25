package au.com.vaadinutils.reportFilter;

import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.ReportFilterUIBuilder;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public class ReportPopUp extends Window implements ReportDataProvider
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

//	static private final transient Logger logger = LogManager.getLogger();

	final ReportViewBase report;
	private List<ReportParameter<?>> filters;
	private ReportDataProvider parentDataProvider;

	protected ReportPopUp(String title, JasperManager manager, List<ReportParameter<?>> filters,
			ReportDataProvider dataProvider)
	{
		this.filters = filters;
		parentDataProvider = dataProvider;
		report = new ReportViewBase(title, manager, this);

		init();

	}

	public void init()
	{
		this.setWidth("90%");
		this.setHeight("80%");
		report.initScreen();
		this.setContent(report);
		setModal(true);
		// center();
		UI.getCurrent().addWindow(this);

	}

	public JasperManager getJasperManager()
	{
		return report.manager;

	}

	@Override
	public ReportFilterUIBuilder getFilterBuilder(JasperManager manager)
	{
		ReportFilterUIBuilder builder = new ReportFilterUIBuilder(manager);

		for (ReportParameter<?> filter : filters)
		{
			builder.addField(filter);
		}

		return builder;
	}

	@Override
	public List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params, String reportFileName) throws Exception
	{
		return parentDataProvider.prepareData(params,reportFileName);
	}

	@Override
	public void cleanup()
	{
		parentDataProvider.cleanup();
		
	}

	@Override
	public void prepareForOutputFormat(OutputFormat outputFormat)
	{
		parentDataProvider.prepareForOutputFormat(outputFormat);
		
	}
	

}
