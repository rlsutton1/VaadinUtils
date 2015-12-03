package au.com.vaadinutils.jasper.ui;

import com.google.common.base.Preconditions;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public abstract class JasperReportView extends HorizontalLayout implements View
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	// static private final transient Logger logger = LogManager.getLogger();

	private JasperReportLayout report = null;

	protected JasperReportView(JasperReportProperties reportProperties)
	{
		report = new JasperReportLayout(reportProperties);
	}

	protected JasperReportView()
	{
	}
	protected void setReport(JasperReportProperties reportProperties)
	{
		report = new JasperReportLayout(reportProperties);
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		Preconditions.checkState(report != null);
		
		this.setSizeFull();
		report.initScreen(new MainReportResizableSplitPanel(235));
		this.addComponent(report);

	}

}
