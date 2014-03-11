package au.com.vaadinutils.reportFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import net.sf.jasperreports.engine.JRException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.RenderedReport;
import au.com.vaadinutils.jasper.ReportFilterUIBuilder;
import au.com.vaadinutils.listener.ClickEventLogged;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public abstract class ReportView extends HorizontalLayout implements View
{
	public static final String NAME = "ReportView";
	private static final long serialVersionUID = 1L;

	static private final Logger logger = LogManager.getLogger();

	private BrowserFrame displayPanel;

	private HorizontalSplitPanel layout;

	String title;
	String servletUrl;

	JasperManager manager;
	private ReportFilterUIBuilder builder;

	protected ReportView(String title, String servletUrl, JasperManager manager)
	{
		this.title = title;
		this.servletUrl = servletUrl;
		this.manager = manager;
		this.builder = getFilterBuilder();
	}

	protected ReportView(String title, JasperManager manager)
	{
		this.title = title;
		this.servletUrl = null;
		this.manager = manager;
		this.builder = getFilterBuilder();
	}

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
		Label label = new Label("<font size='4' >Set the desired filters and click 'Apply' to generate a report</font>");
		label.setContentMode(ContentMode.HTML);
		splash.addComponent(label);

		layout.setSecondComponent(splash);
		this.addComponent(layout);

		if (!this.builder.hasFilters())
			try
			{
				generateReport(JasperManager.OutputFormat.HTML, null);
			}
			catch (JRException e)
			{
				logger.catching(e);
				Notification.show("Error", e.getMessage(), Type.ERROR_MESSAGE);
			}
			catch (IOException e)
			{
				logger.catching(e);
				Notification.show("Error", e.getMessage(), Type.ERROR_MESSAGE);
			}

	}

	public Component getOptionsPanel()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setHeight("100%");
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		Label titleLabel = new Label("<h1>" + title + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		layout.addComponent(titleLabel);

		Label description = new Label("<p>" + getDescription() + "</p>");
		description.setContentMode(ContentMode.HTML);
		layout.addComponent(description);

		AbstractLayout filterLayout = builder.buildLayout();
		layout.addComponent(filterLayout);
		layout.setExpandRatio(filterLayout, 1.0f);

		ComboBoxAction actions[] = new ComboBoxAction[]
		{ new ShowAction(), new PrintQualityAction(), new CSVExportAction() };
		final ComboBox actionCombo = new ComboBox("Action", Arrays.asList(actions));
		actionCombo.setWidth("120px");
		actionCombo.setNullSelectionAllowed(false);
		actionCombo.setValue(actions[0]);
		actionCombo.setNewItemsAllowed(false);
		actionCombo.setTextInputAllowed(false);

		Button applyButton = new Button("Apply");
		applyButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				ComboBoxAction action = (ComboBoxAction) actionCombo.getValue();
				try
				{
					action.exec();
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		});

		layout.addComponent(actionCombo);
		layout.addComponent(applyButton);

		return layout;
	}

	class ShowAction implements ComboBoxAction
	{
		@Override
		public void exec() throws Exception
		{
			generateReport(OutputFormat.HTML, ReportView.this.builder.getReportParameters());
		}

		public String toString()
		{
			return "Show";
		}
	}

	class PrintQualityAction implements ComboBoxAction
	{
		@Override
		public void exec() throws Exception
		{
			generateReport(OutputFormat.PDF, ReportView.this.builder.getReportParameters());
		}

		public String toString()
		{
			return "Print Quality";
		}
	}

	class CSVExportAction implements ComboBoxAction
	{
		@Override
		public void exec() throws Exception
		{
			generateReport(OutputFormat.CSV, ReportView.this.builder.getReportParameters());
		}

		public String toString()
		{
			return "Export to CSV";
		}

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
		displayPanel = new BrowserFrame("Report Display");
		displayPanel.setSizeFull();
		displayPanel.setStyleName("njadmin-hide-overflow-for-help");
		layout.setSecondComponent(displayPanel);
		return displayPanel;

	}

	protected void generateReport(JasperManager.OutputFormat outputFormat, Collection<ReportParameter<?>> params)
			throws JRException, IOException
	{
		if (this.servletUrl == null)
		{
			if (params != null)
			{
				for (ReportParameter<?> param : params)
				{
					this.manager.bindParameter(param.parameterName, param.getValue());
				}
			}
			showReport(this.manager.export(outputFormat));
		}
		else
		{
			String target = servletUrl;
			target += "?OutputFormat=" + outputFormat.toString();
			target += "&ReportName=" + java.net.URLEncoder.encode(manager.getReportName(), "UTF-8");
			target += "&ReportTitle=" + java.net.URLEncoder.encode(title, "UTF-8");
			target += "&uniqueifier=" + System.currentTimeMillis();

			if (params != null)
			{
				for (ReportParameter<?> param : params)
				{
					target += "&" + param.getUrlEncodedKeyAndParameter();
				}
			}

			logger.debug(target);
			showReport(target);
		}
	}

	interface ComboBoxAction
	{
		public void exec() throws Exception;
	}

	public JasperManager getJasperManager()
	{
		return this.manager;

	}

	/**
	 * Overload this method if you report needs to be filtered.
	 * 
	 * @return
	 */
	protected ReportFilterUIBuilder getFilterBuilder()
	{
		return new ReportFilterUIBuilder(manager);
	}

}
