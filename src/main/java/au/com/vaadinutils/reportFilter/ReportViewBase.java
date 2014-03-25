package au.com.vaadinutils.reportFilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.jasperreports.engine.JRException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.jasper.CustomJRHyperlinkProducerFactory;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.RenderedReport;
import au.com.vaadinutils.jasper.ReportFilterUIBuilder;
import au.com.vaadinutils.listener.ClickEventLogged;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
public class ReportViewBase extends HorizontalSplitPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4195751089112256038L;

	public static final String NAME = "ReportView";

	static private final transient Logger logger = LogManager.getLogger();

	private BrowserFrame displayPanel;

	String title;

	JasperManager manager;
	private ReportFilterUIBuilder builder;

	private ReportDataProvider dataProvider;

	protected ReportViewBase(String title, JasperManager manager, ReportDataProvider dataProvider)
	{
		this.title = title;
		this.manager = manager;
		this.builder = dataProvider.getFilterBuilder(manager);
		this.dataProvider = dataProvider;
	}

	public void initScreen()
	{
		this.setSizeFull();

		this.setSplitPosition(20);
		this.setFirstComponent(getOptionsPanel());
		if (!builder.hasFilters())
		{
			setSplitPosition(10);
		}

		VerticalLayout splash = new VerticalLayout();
		splash.setMargin(true);

		Label titleLabel = new Label("<h1>" + title + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		splash.addComponent(titleLabel);

		Label label = new Label("<font size='4' >Set the desired filters and click 'Apply' to generate a report</font>");
		label.setContentMode(ContentMode.HTML);
		splash.addComponent(label);

		this.setSecondComponent(splash);

		if (!this.builder.hasFilters())
		{
			try
			{
				generateReport(JasperManager.OutputFormat.HTML, ReportViewBase.this.builder.getReportParameters());
			}
			catch (Exception e)
			{
				logger.catching(e);
				Notification.show("Error", e.getMessage(), Type.ERROR_MESSAGE);
			}
		}

		// window.parent.au.com.noojee.reportDrillDown(
		// {
		// 'reportFileName':
		// 'CallDetailsPerTeamAgentPerHour_CallDetails.jasper',
		// 'reportTitle': 'Call Details Per Team Agent Per Hour'
		// },
		// {
		// 'ReportParameterStartDate'='$P{StartDate}',
		// 'ReportParameterEndDate'='$P{EndDate}',
		// 'ReportParameterExtension'='$F{loginid}',
		// 'ReportParameterTeamId'='$P{TeamId}',
		// 'ReportParameterHour'='$F{Day}.toString()'
		// }
		//
		// );

		JavaScript.getCurrent().addFunction("au.com.noojee.reportDrillDown", new JavaScriptFunction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void call(JSONArray arguments) throws JSONException
			{
				try
				{
					JSONObject args = arguments.getJSONObject(0);
					String subReportFileName = args.get("ReportFileName").toString();
					String subTitle = args.get("ReportTitle").toString();

					JSONObject params = arguments.getJSONObject(1);

					JasperManager subManager = new JasperManager(EntityManagerProvider.getEntityManager(),
							subReportFileName, manager.getSettings());

					List<ReportParameter<?>> subFilters = new LinkedList<ReportParameter<?>>();

					@SuppressWarnings("unchecked")
					Iterator<String> itr = params.keys();
					while (itr.hasNext())
					{
						String key = itr.next();
						subFilters.add(new ReportParameterConstant(key, params.getString(key)));
					}

					new ReportPopUp(subTitle, subManager, subFilters, dataProvider);
				}
				catch (Exception e)
				{
					logger.error(arguments.toString());
					logger.error(e, e);
				}
			}
		});

	}

	public Component getOptionsPanel()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setHeight("100%");
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();

		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSpacing(true);
		buttonBar.setStyleName(Reindeer.LAYOUT_BLACK);
		buttonBar.setWidth("100");
		buttonBar.setHeight("20");

		buttonBar.setMargin(new MarginInfo(false, true, false, true));

		Button showButton = new Button();
		showButton.setIcon(new ExternalResource("images/famfamicons/monitor.png"));
		showButton.setStyleName(Reindeer.BUTTON_LINK);
		// showButton.setWidth("70");
		addButtonListener(showButton, OutputFormat.HTML);
		buttonBar.addComponent(showButton);

		Button printButton = new Button();
		printButton.setIcon(new ExternalResource("images/famfamicons/printer.png"));
		printButton.setStyleName(Reindeer.BUTTON_LINK);
		addButtonListener(printButton, OutputFormat.PDF);
		buttonBar.addComponent(printButton);

		Button exportButton = new Button();
		exportButton.setIcon(new ExternalResource("images/famfamicons/cd.png"));
		// exportButton.setWidth("45");
		exportButton.setStyleName(Reindeer.BUTTON_LINK);
		addButtonListener(exportButton, OutputFormat.CSV);
		buttonBar.addComponent(exportButton);

		layout.addComponent(buttonBar);

		AbstractLayout filterLayout = builder.buildLayout();
		layout.addComponent(filterLayout);
		layout.setExpandRatio(filterLayout, 1.0f);

		return layout;
	}

	void addButtonListener(Button button, final OutputFormat format)
	{
		button.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				try
				{
					generateReport(format, ReportViewBase.this.builder.getReportParameters());
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
		});
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
		this.setSecondComponent(displayPanel);
		return displayPanel;

	}

	protected void generateReport(JasperManager.OutputFormat outputFormat, Collection<ReportParameter<?>> params)
			throws Exception
	{
		if (params == null)
		{
			params = new LinkedList<ReportParameter<?>>();
		}
		params.addAll(dataProvider.prepareData(params,manager.getReportFilename()));

		logger.warn("Running report "+manager.getReportFilename());
		for (ReportParameter<?> param : params)
		{
			this.manager.bindParameter(param.parameterName, param.getValue());
			logger.warn(param.parameterName+ " "+param.getValue());
		}

		try
		{
			dataProvider.prepareForOutputFormat(outputFormat);
			CustomJRHyperlinkProducerFactory.setUseCustomHyperLinks(true);
			showReport(this.manager.export(outputFormat));
		}
		finally
		{
			CustomJRHyperlinkProducerFactory.setUseCustomHyperLinks(false);
			dataProvider.cleanup();
		}
	}

}
