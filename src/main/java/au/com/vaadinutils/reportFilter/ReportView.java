package au.com.vaadinutils.reportFilter;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/** A start view for navigating to the main view */
public abstract class ReportView extends HorizontalLayout implements View
{
	public static final String NAME = "ReportView";

	private static final long serialVersionUID = 1L;

	private BrowserFrame pdfPanel;

	private HorizontalSplitPanel layout;

	@Override
	public void enter(ViewChangeEvent event)
	{
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

	public void showReport(String url)
	{
		ExternalResource source = new ExternalResource(url);
		// source.setMIMEType("application/pdf");
		if (pdfPanel == null)
		{
			layout.setSecondComponent(getPdfPanel());
		}
		pdfPanel.setSource(source);

	}

	private Component getPdfPanel()
	{
		if (pdfPanel == null)
		{
			pdfPanel = new BrowserFrame("Preview");
			pdfPanel.setSizeFull();
			pdfPanel.setStyleName("njadmin-hide-overflow-for-help");
		}
		return pdfPanel;

	}

	public abstract Component getOptionsPanel();

}
