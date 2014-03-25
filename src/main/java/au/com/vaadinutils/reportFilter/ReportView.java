package au.com.vaadinutils.reportFilter;

import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.ReportFilterUIBuilder;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public abstract class ReportView extends HorizontalLayout implements View, ReportDataProvider
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	//static private final transient Logger logger = LogManager.getLogger();

	final ReportViewBase report;



	protected ReportView(String title, JasperManager manager)
	{
		report = new ReportViewBase(title, manager, this);
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		this.setSizeFull();
		report.initScreen();
		this.addComponent(report);

	}

	public JasperManager getJasperManager()
	{
		return report.manager;

	}

	public abstract ReportFilterUIBuilder getFilterBuilder(JasperManager manager);

	public abstract List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params,String reportFileName) throws Exception;

}
