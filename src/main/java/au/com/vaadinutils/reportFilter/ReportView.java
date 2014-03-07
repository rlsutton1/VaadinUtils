package au.com.vaadinutils.reportFilter;

import au.com.vaadinutils.jasper.RenderedReport;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/** Base class for a view that provides a report filter selection area and a report viewing area. */
public abstract class ReportView extends HorizontalLayout implements View
{
	public static final String NAME = "ReportView";

	private static final long serialVersionUID = 1L;

	private BrowserFrame displayPanel;

	private HorizontalSplitPanel layout;

	@Override
	public void enter(ViewChangeEvent event)
	{
		this.setSizeFull();
		layout = new HorizontalSplitPanel();
		layout.setSizeFull();

		layout.setSplitPosition(20);
		layout.setFirstComponent(getOptionsPanel());
		
		VerticalLayout splash = new VerticalLayout();
		splash.setMargin(true);
		Label label = new Label("<font size='4' > Select filters and click 'Show' to generate a report</font>");
		splash.addComponent(label);
		label.setContentMode(ContentMode.HTML);
		layout.setSecondComponent(splash);
		this.addComponent(layout);

	}

	public void showReport(RenderedReport report)
	{
		Resource resource = report.getBodyAsResource();
		getDisplayPanel().setSource(resource);
	}
	
	public void showReport(String url)
	{
		ExternalResource source = new ExternalResource(url);
		// source.setMIMEType("application/pdf");
		getDisplayPanel().setSource(source);

	}

	private BrowserFrame getDisplayPanel()
	{
		if (displayPanel == null)
		{
			displayPanel = new BrowserFrame("Report Display");
			displayPanel.setSizeFull();
			displayPanel.setStyleName("njadmin-hide-overflow-for-help");
			layout.setSecondComponent(displayPanel);
		}
		return displayPanel;

	}

	public abstract Component getOptionsPanel();

}
