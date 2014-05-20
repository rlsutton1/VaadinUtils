package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public class JasperReportPopUp extends Window 
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	// static private final transient Logger logger = LogManager.getLogger();

	final JasperReportLayout report;

	public JasperReportPopUp(JasperReportProperties reportPropertiesTemplate)
	{

		report = new JasperReportLayout(reportPropertiesTemplate);

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

	
}
