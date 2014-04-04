package au.com.vaadinutils.jasper.ui;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.JasperProgressListener;
import au.com.vaadinutils.jasper.filter.ExpanderComponent;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.parameter.ReportParameterConstant;
import au.com.vaadinutils.listener.CancelListener;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.ui.WorkingDialog;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.google.common.base.Preconditions;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
class JasperReportLayout extends HorizontalSplitPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4195751089112256038L;

	public static final String NAME = "ReportView";

	static private final transient Logger logger = LogManager.getLogger();

	private BrowserFrame displayPanel;

	JasperManager manager;
	private ReportFilterUIBuilder builder;

	private List<ExpanderComponent> components;

	private NativeButton showButton;

	private NativeButton printButton;

	private NativeButton exportButton;

	private VerticalLayout splash;

	private JasperReportProperties reportProperties;

	private BrowserFrame csv;

	protected JasperReportLayout(JasperReportProperties reportProperties)
	{
		this.reportProperties = reportProperties;
		this.manager = new JasperManager(reportProperties);
		this.builder = reportProperties.getDataProvider().getFilterBuilder(manager);
	}

	protected void initScreen()
	{
		this.setSizeFull();

		this.setSplitPosition(20);
		this.setFirstComponent(getOptionsPanel());
		if (!builder.hasFilters())
		{
			setSplitPosition(15);

		}else
		{
			Integer splitAt = builder.getMinWidth();
			if (splitAt == null)
			{
				splitAt = 15;
			}
			setSplitPosition(splitAt);

		}

		splash = new VerticalLayout();
		splash.setMargin(true);

		Label titleLabel = new Label("<h1>" + reportProperties.getReportTitle() + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		splash.addComponent(titleLabel);

		Label label = new Label("<font size='4' >Set the desired filters and click 'Apply' to generate a report</font>");
		label.setContentMode(ContentMode.HTML);
		splash.addComponent(label);

		this.setSecondComponent(splash);

		// generate the report immediately if there are no visible filters
		if (!builder.hasFilters())
		{
			// disable the buttons and any filters
			printButton.setEnabled(false);
			exportButton.setEnabled(false);
			showButton.setEnabled(false);
			for (ExpanderComponent componet : components)
			{
				componet.getComponent().setEnabled(false);
			}

			// what follows is a horrible hack...

			// if we create the progress dialog at the same time as the popup
			// report window the progress dialog will be behind the popup report
			// window.
			// so I've created a refresher, and 1 seconds after the popup report
			// window opens we kick of the report generation which creates the
			// progress dialog then, which allows it to be in front.

			final Refresher refresher = new Refresher();
			refresher.addListener(new RefreshListener()
			{

				private static final long serialVersionUID = -5629950903521881207L;

				@Override
				public void refresh(Refresher source)
				{

					try
					{
						removeExtension(refresher);
						generateReport(reportProperties.getDataProvider().getDefaultFormat(),
								JasperReportLayout.this.builder.getReportParameters());

					}
					catch (Exception e)
					{
						logger.catching(e);
						Notification.show("Error", e.getMessage(), Type.ERROR_MESSAGE);
					}

				}
			});
			addExtension(refresher);
		}

		JavaScript.getCurrent().addFunction("au.com.noojee.reportDrillDown", new JavaScriptFunction()
		{

			// expected syntax of a call to this javascript hook method
			//
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

					JasperReportProperties subReportProperties = new JasperReportProperties(subTitle,
							subReportFileName, reportProperties.getDataProvider(), reportProperties.getEm(),
							reportProperties.getSettings());

					List<ReportParameter<?>> subFilters = new LinkedList<ReportParameter<?>>();

					@SuppressWarnings("unchecked")
					Iterator<String> itr = params.keys();
					while (itr.hasNext())
					{
						String key = itr.next();
						subFilters.add(new ReportParameterConstant(key, params.getString(key)));
					}

					new JasperReportPopUp(subReportProperties, subFilters);
				}
				catch (Exception e)
				{
					logger.error(arguments.toString());
					logger.error(e, e);
				}
			}
		});

	}

	private Component getOptionsPanel()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(new MarginInfo(false, false, false, false));
		layout.setSpacing(true);
		layout.setSizeFull();
		

		String buttonHeight = "" + 40;
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSpacing(true);
		buttonBar.setStyleName(Reindeer.LAYOUT_BLUE);
		buttonBar.setWidth("100%");
		buttonBar.setHeight(buttonHeight);

		buttonBar.setMargin(new MarginInfo(false, true, false, true));

		showButton = new NativeButton();
		showButton.setIcon(new ExternalResource("images/seanau/Preview_32.png"));
		showButton.setDescription("Preview");
		showButton.setWidth("50");
		showButton.setHeight(buttonHeight);
		showButton.setDisableOnClick(true);
		addButtonListener(showButton, OutputFormat.HTML);
		buttonBar.addComponent(showButton);

		printButton = new NativeButton();
		printButton.setIcon(new ExternalResource("images/seanau/Print_32.png"));
		printButton.setDescription("Print (PDF)");
		printButton.setWidth("50");
		printButton.setHeight(buttonHeight);
		printButton.setDisableOnClick(true);
		addButtonListener(printButton, OutputFormat.PDF);
		buttonBar.addComponent(printButton);

		exportButton = new NativeButton();
		exportButton.setDescription("Export (Excel - CSV)");
		exportButton.setIcon(new ExternalResource("images/exporttoexcel.png"));
		exportButton.setWidth("50");
		exportButton.setDisableOnClick(true);
		exportButton.setHeight(buttonHeight);
		// exportButton.setStyleName(Reindeer.BUTTON_LINK);
		addButtonListener(exportButton, OutputFormat.CSV);
		buttonBar.addComponent(exportButton);

		layout.addComponent(buttonBar);

		components = builder.buildLayout();
		if (components.size() > 0)
		{
			VerticalLayout filterPanel = new VerticalLayout();
			filterPanel.setMargin(true);
			filterPanel.setSpacing(true);
			// filterPanel.setSizeFull();
			Label filterLabel = new Label("<b>Filters</b>");
			filterLabel.setContentMode(ContentMode.HTML);
			filterPanel.addComponent(filterLabel);

			for (ExpanderComponent componet : components)
			{
				filterPanel.addComponent(componet.getComponent());
				if (componet.shouldExpand())
				{
					filterPanel.setExpandRatio(componet.getComponent(), 1);
				}
			}
			layout.addComponent(filterPanel);
			layout.setExpandRatio(filterPanel, 1.0f);
		}

		// hidden frame for downloading csv
		csv = new BrowserFrame();
		csv.setVisible(true);
		csv.setHeight("0");
		csv.setWidth("0");
		csv.setImmediate(true);
		layout.addComponent(csv);

		return layout;
	}

	private void addButtonListener(Button button, final OutputFormat format)
	{
		button.addClickListener(new ClickEventLogged.ClickListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void clicked(ClickEvent event)
			{
				try
				{

					printButton.setEnabled(false);
					exportButton.setEnabled(false);
					showButton.setEnabled(false);
					for (ExpanderComponent componet : components)
					{
						componet.getComponent().setEnabled(false);
					}

					generateReport(format, JasperReportLayout.this.builder.getReportParameters());

				}
				catch (Exception e)
				{
					printButton.setEnabled(true);
					exportButton.setEnabled(true);
					showButton.setEnabled(true);
					for (ExpanderComponent componet : components)
					{
						componet.getComponent().setEnabled(true);
					}
					Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
					logger.error(e, e);
				}
				finally
				{

				}
			}
		});
	}

	private BrowserFrame getDisplayPanel()
	{
		displayPanel = new BrowserFrame("Report Display");
		displayPanel.setSizeFull();
		displayPanel.setStyleName("njadmin-hide-overflow-for-help");
		displayPanel.setImmediate(true);
		this.setSecondComponent(displayPanel);
		return displayPanel;

	}

	private boolean cancelled;

	volatile boolean streamReady = false;
	volatile boolean streamConnected = false;

	protected void generateReport(final JasperManager.OutputFormat outputFormat,
			final Collection<ReportParameter<?>> params)

	{
		streamConnected = false;
		streamReady = false;

		manager = null;
		// if there is a report chooser parameter then swap out the report
		// manager for the selected report.
		for (ReportParameter<?> p : params)
		{
			if (p instanceof ReportChooser)
			{
				ReportChooser chooser = (ReportChooser) p;
				manager = new JasperManager(chooser.getReportProperties(reportProperties.getDataProvider()));
				Preconditions.checkNotNull(manager, "chooser returned a NULL JasperManager.");
				break;
			}
		}
		if (manager == null)
		{
			this.manager = new JasperManager(reportProperties);
		}

		CancelListener cancelListener = getProgressDialogCancelListener();
		final WorkingDialog dialog = new WorkingDialog("Generating report", "Please wait", cancelListener);
		dialog.setWidth("400");

		UI.getCurrent().addWindow(dialog);

		// add refresher component
		final Refresher refresher = new Refresher();
		refresher.setRefreshInterval(200);
		addExtension(refresher);

		RefreshListener refreshListener = getProgressDialogRefreshListener(dialog, refresher);
		refresher.addListener(refreshListener);

		RefreshListener streamListener = getStreamConnectorRefreshListener(outputFormat);
		refresher.addListener(streamListener);

		JasperProgressListener listener = getJasperManagerProgressListener(dialog, refresher);
		manager.exportAsync(outputFormat, params, listener);

	}

	private JasperProgressListener getJasperManagerProgressListener(final WorkingDialog dialog,
			final Refresher refresher)
	{
		JasperProgressListener listener = new JasperProgressListener()
		{

			@Override
			public void outputStreamReady()
			{
				// flag that the stream is ready, and on the next
				// "Refresher call" it will connect to the stream
				streamReady = true;

			}

			@Override
			public void failed(String string)
			{
				// show the error message
				Notification.show(string, Type.ERROR_MESSAGE);
				reportFinished();

			}

			private void reportFinished()
			{
				// re-enable fields after report finished.
				printButton.setEnabled(true);
				exportButton.setEnabled(true);
				showButton.setEnabled(true);
				for (ExpanderComponent componet : components)
				{
					componet.getComponent().setEnabled(true);
				}
				removeExtension(refresher);
				dialog.close();
			}

			@Override
			public void completed()
			{
				reportFinished();
			}
		};
		return listener;
	}

	private RefreshListener getStreamConnectorRefreshListener(final JasperManager.OutputFormat outputFormat)
	{
		RefreshListener streamListener = new RefreshListener()
		{

			private static final long serialVersionUID = -2042404606367957120L;

			@Override
			public void refresh(Refresher source)
			{
				if (streamReady && !streamConnected)
				{
					// jasper manager is ready, so get the report stream and set
					// it as the source for the display panel
					streamConnected = true;
					StreamResource resource = new StreamResource(getReportStream(), "report");
					resource.setMIMEType(outputFormat.getMimeType());
					resource.setCacheTime(-1);
					resource.setFilename(reportProperties.getReportTitle() + "-" + System.currentTimeMillis()
							+ outputFormat.getFileExtension());

					if (outputFormat == OutputFormat.CSV)
					{
						csv.setSource(resource);
						showCsvSplash();
					}
					else
					{
						getDisplayPanel().setSource(resource);
					}
				}

			}
		};
		return streamListener;
	}

	private void showCsvSplash()
	{
		VerticalLayout csvSplash = new VerticalLayout();
		csvSplash.setMargin(true);

		Label titleLabel = new Label("<h1>" + reportProperties.getReportTitle() + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		csvSplash.addComponent(titleLabel);

		Label label = new Label("<font size='4' >Excel (CSV) download will initiated " + new Date() + ".</font>");
		label.setContentMode(ContentMode.HTML);
		csvSplash.addComponent(label);

		this.setSecondComponent(csvSplash);

	}

	private StreamSource getReportStream()
	{
		@SuppressWarnings("serial")
		StreamSource source = new StreamSource()
		{

			@Override
			public InputStream getStream()
			{
				try
				{
					return manager.getStream();
				}
				catch (InterruptedException e)
				{
					logger.error(e, e);
				}
				return null;
			}
		};
		return source;
	}

	private RefreshListener getProgressDialogRefreshListener(final WorkingDialog dialog, final Refresher refresher)
	{
		RefreshListener refreshListener = new RefreshListener()
		{

			private static final long serialVersionUID = -5641305025399715756L;

			@Override
			public void refresh(Refresher source)
			{
				dialog.progress(0, 0, manager.getStatus());
				if (cancelled)
				{
					printButton.setEnabled(true);
					exportButton.setEnabled(true);
					showButton.setEnabled(true);
					for (ExpanderComponent componet : components)
					{
						componet.getComponent().setEnabled(true);
					}
					removeExtension(refresher);
					dialog.close();
				}
			}
		};
		return refreshListener;
	}

	private CancelListener getProgressDialogCancelListener()
	{
		cancelled = false;
		CancelListener cancelListener = new CancelListener()
		{

			@Override
			public void cancel()
			{
				manager.cancelPrint();
				cancelled = true;

				// change to the splash page as the report may still complete
				JasperReportLayout.this.setSecondComponent(splash);
			}
		};
		return cancelListener;
	}

}
