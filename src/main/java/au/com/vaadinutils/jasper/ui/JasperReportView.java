package au.com.vaadinutils.jasper.ui;

import java.util.Collection;
import java.util.List;

import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public abstract class JasperReportView extends HorizontalLayout implements View, JasperReportDataProvider
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	//static private final transient Logger logger = LogManager.getLogger();

	final JasperReportLayout report;



	protected JasperReportView(String title, JasperManager manager)
	{
		report = new JasperReportLayout(title, manager, this);
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		this.setSizeFull();
		report.initScreen();
		this.addComponent(report);

	}

	public abstract ReportFilterUIBuilder getFilterBuilder(JasperManager manager);

	public abstract List<ReportParameter<?>> prepareData(Collection<ReportParameter<?>> params,String reportFileName) throws Exception;

}
