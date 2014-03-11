package au.com.vaadinutils.jasper;

import java.io.IOException;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import au.com.vaadinutils.reportFilter.ReportParameter;

import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Creates a new window with a Jasper report ready to print.
 * 
 * Its assumed that any parameters have already been passed to Jasper and it is
 * ready to print
 * 
 * @author bsutton
 * 
 */
public class PrintWindow extends Window
{
	private static final long serialVersionUID = 1L;

	private BrowserFrame displayPanel;

	private String title;
	private JasperManager manager;

	public PrintWindow(JasperManager manager)
	{
		this.title = manager.getReportName();
		this.manager = manager;

		this.setContent(buildLayout());

		this.setWidth(UI.getCurrent().getWidth() * 0.6f, UI.getCurrent().getWidthUnits());
		// this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.center();
	}

	public AbstractLayout buildLayout()
	{
		this.setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);

		layout.addComponent(getOptionsPanel());

		try
		{
			generateReport(JasperManager.OutputFormat.PDF, null);
		}
		catch (Exception e)
		{
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}

		return layout;
	}

	public Component getOptionsPanel()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setHeight("100%");
//		layout.setMargin(true);
//		layout.setSpacing(true);
		layout.setSizeFull();
		Label titleLabel = new Label("<h1>" + title + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		layout.addComponent(titleLabel);
		BrowserFrame panel = getDisplayPanel();
		layout.addComponent(panel);
		layout.setExpandRatio(panel, 1.0f);

		return layout;
	}

	public void showReport(RenderedReport report)
	{
		Resource resource = report.getBodyAsResource();
		getDisplayPanel().setSource(resource);
	}

	private BrowserFrame getDisplayPanel()
	{
		if (displayPanel == null)
		{
			displayPanel = new BrowserFrame();
			displayPanel.setSizeFull();
			displayPanel.setStyleName("njadmin-hide-overflow-for-help");
		}
		return displayPanel;

	}

	protected void generateReport(JasperManager.OutputFormat outputFormat, List<ReportParameter<?>> params)
			throws JRException, IOException
	{
		if (this.manager != null)
		{
			showReport(this.manager.export(outputFormat));
		}
	}
}
