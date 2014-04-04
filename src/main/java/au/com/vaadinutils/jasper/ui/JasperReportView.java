package au.com.vaadinutils.jasper.ui;

import javax.persistence.EntityManager;

import au.com.vaadinutils.jasper.JasperSettings;

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

	// static private final transient Logger logger = LogManager.getLogger();

	final JasperReportLayout report;

	protected JasperReportView(JasperReportProperties reportProperties)
	{
		report = new JasperReportLayout(reportProperties);
	}

	protected JasperReportView(String title, String filename, EntityManager entityManager, JasperSettings settings)
	{
		report = new JasperReportLayout(new JasperReportProperties(title, filename, this, entityManager, settings));
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		this.setSizeFull();
		report.initScreen();
		this.addComponent(report);

	}

}
