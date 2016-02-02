package au.com.vaadinutils.jasper.ui;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.data.Item;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.JasperProgressListener;
import au.com.vaadinutils.jasper.QueueEntry;
import au.com.vaadinutils.jasper.ReportStatus;
import au.com.vaadinutils.jasper.filter.ExpanderComponent;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.parameter.ReportParameterConstant;
import au.com.vaadinutils.jasper.scheduler.JasperReportEmailWindow;
import au.com.vaadinutils.jasper.scheduler.JasperReportSchedulerWindow;
import au.com.vaadinutils.jasper.scheduler.ScheduleIconBuilder;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity_;
import au.com.vaadinutils.listener.CancelListener;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.ui.WorkingDialog;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Base class for a view that provides a report filter selection area and a
 * report viewing area.
 */
class JasperReportLayout extends VerticalLayout
{
	/**
	 *
	 */
	private static final long serialVersionUID = -4195751089112256038L;

	public static final String NAME = "ReportView";

	static private final transient Logger logger = LogManager.getLogger();

	private static final String PRINT_PANEL_ID = "nj-print-panel-id-for-pdf";

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

	private SplitPanel splitPanel;

	private NativeButton scheduleButton;

	private NativeButton emailButton;

	protected JasperReportLayout(JasperReportProperties reportProperties)
	{
		this.reportProperties = reportProperties;
		this.builder = reportProperties.getFilterBuilder();
	}

	protected void initScreen(SplitPanel panel)
	{
		this.setSizeFull();

		splitPanel = panel;
		this.addComponent(splitPanel.getComponent());

		splitPanel.setFirstComponent((AbstractComponent) getOptionsPanel());

		splash = new VerticalLayout();
		splash.setMargin(true);

		Label titleLabel = new Label("<h1>" + reportProperties.getReportTitle() + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		splash.addComponent(titleLabel);

		Label splashLabel = new Label(
				"<font size='4' >Set the desired filters and click a print button to generate a report</font>");
		splashLabel.setContentMode(ContentMode.HTML);

		splitPanel.setSecondComponent(splash);

		// generate the report immediately if there are no visible filters
		if (!builder.hasFilters())
		{
			splashLabel = new Label("<font size='4' >Please wait whilst we generate your report</font>");
			splashLabel.setContentMode(ContentMode.HTML);

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

			UI.getCurrent().setPollInterval(500);
			UI.getCurrent().addPollListener(new PollListener()
			{

				private static final long serialVersionUID = 1L;

				@Override
				public void poll(PollEvent event)
				{

					try
					{
						UI.getCurrent().setPollInterval(-1);
						UI.getCurrent().removePollListener(this);
						generateReport(reportProperties.getDefaultFormat(),
								JasperReportLayout.this.builder.getReportParameters());

					}
					catch (Exception e)
					{
						logger.catching(e);
						Notification.show("Error", e.getMessage(), Type.ERROR_MESSAGE);
					}

				}
			});

		}
		splash.addComponent(splashLabel);

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
			public void call(JsonArray arguments)
			{

				try
				{
					JsonObject args = arguments.getObject(0);
					String subReportFileName = args.getString("ReportFileName");
					String subTitle = args.getString("ReportTitle");

					JsonObject params = arguments.getObject(1);

					List<ReportParameter<?>> subFilters = new LinkedList<ReportParameter<?>>();

					boolean insitue = false;
					String[] itr = params.keys();
					for (String key : itr)
					{
						if (key.equalsIgnoreCase("ReportParameterInsitue"))
						{
							insitue = true;
						}
						else
						{
							subFilters.add(new ReportParameterConstant<String>(key, params.getString(key), key,
									params.getString(key)));
						}
					}

					if (!insitue)
					{
						new JasperReportPopUp(new ChildJasperReportProperties(reportProperties, subTitle,
								subReportFileName, subFilters));
					}
					else
					{
						generateReport(OutputFormat.HTML, subFilters);
					}
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
		layout.setId("OptionsPanel");
		// layout.setMargin(new MarginInfo(false, false, false, false));
		// layout.setSpacing(true);
		layout.setSizeFull();

		String buttonHeight = "" + 40;
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSpacing(true);
		buttonBar.setStyleName(Reindeer.LAYOUT_BLUE);
		buttonBar.setWidth("100%");
		buttonBar.setHeight(buttonHeight);

		buttonBar.setMargin(new MarginInfo(false, false, false, false));

		HorizontalLayout buttonContainer = new HorizontalLayout();
		buttonContainer.setSizeFull();
		buttonContainer.setWidth("230");

		showButton = new NativeButton();
		showButton.setIcon(new ExternalResource("images/seanau/Print preview.png"));
		showButton.setDescription("Preview");
		showButton.setWidth("50");
		showButton.setHeight(buttonHeight);
		showButton.setDisableOnClick(true);
		addButtonListener(showButton, OutputFormat.HTML);
		buttonContainer.addComponent(showButton);

		printButton = new NativeButton();
		printButton.setIcon(new ExternalResource("images/seanau/Print_32.png"));
		printButton.setDescription("Print (PDF)");
		printButton.setWidth("50");
		printButton.setHeight(buttonHeight);
		printButton.setDisableOnClick(true);
		addButtonListener(printButton, OutputFormat.PDF);
		buttonContainer.addComponent(printButton);

		exportButton = new NativeButton();
		exportButton.setDescription("Export (Excel - CSV)");
		exportButton.setIcon(new ExternalResource("images/exporttoexcel.png"));
		exportButton.setWidth("50");
		exportButton.setDisableOnClick(true);
		exportButton.setHeight(buttonHeight);
		// exportButton.setStyleName(Reindeer.BUTTON_LINK);
		addButtonListener(exportButton, OutputFormat.CSV);
		buttonContainer.addComponent(exportButton);

		createEmailButton(buttonHeight, buttonContainer);
		createScheduleButton(buttonHeight, buttonContainer);
		if (reportProperties instanceof JasperReportPopUp)
		{
			// This is disabled because there are serious problems with
			// transient (JasperReportProperties is not aware of them)
			// parameters in drill
			// downs, these can not currently be save or represented in the
			// ReportEmailSchedule
			emailButton.setEnabled(false);
			scheduleButton.setEnabled(false);
		}

		buttonBar.addComponent(buttonContainer);
		layout.addComponent(buttonBar);

		components = builder.buildLayout(false);
		if (components.size() > 0)
		{
			VerticalLayout filterPanel = new VerticalLayout();
			filterPanel.setMargin(new MarginInfo(false, false, true, false));
			filterPanel.setSpacing(true);
			filterPanel.setSizeFull();
			Label filterLabel = new Label("<b>Filters</b>");
			filterLabel.setStyleName(Reindeer.LABEL_H2);
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
		csv.setHeight("1");
		csv.setWidth("1");
		csv.setImmediate(true);
		layout.addComponent(csv);

		return layout;
	}

	private void createEmailButton(String buttonHeight, HorizontalLayout buttonContainer)
	{
		emailButton = new NativeButton();
		emailButton.setIcon(new ExternalResource("images/seanau/Send Email_32.png"));
		emailButton.setDescription("Email");
		emailButton.setWidth("50");
		emailButton.setHeight(buttonHeight);
		emailButton.addClickListener(new ClickEventLogged.ClickListener()
		{

			private static final long serialVersionUID = 7207441556779172217L;

			@Override
			public void clicked(ClickEvent event)
			{
				new JasperReportEmailWindow(reportProperties, builder.getReportParameters());
			}
		});
		buttonContainer.addComponent(emailButton);
	}

	private void createScheduleButton(String buttonHeight, HorizontalLayout buttonContainer)
	{
		scheduleButton = new NativeButton();

		JpaBaseDao<ReportEmailScheduleEntity, Long> dao = JpaBaseDao.getGenericDao(ReportEmailScheduleEntity.class);
		Long count = dao.getCount(ReportEmailScheduleEntity_.JasperReportPropertiesClassName,
				reportProperties.getReportClass().getCanonicalName());

		ScheduleIconBuilder iconBuilder = new ScheduleIconBuilder();

		String baseIconFileName = "Call Calendar_32";
		String path = VaadinServlet.getCurrent().getServletContext().getRealPath("templates/images/seanau/");

		// HACK: scoutmaster stores images in a different directory so if the
		// images isn't found in the above templates directory
		// then search in the /images/seanau director.
		if (path == null || !new File(path).exists())
			path = VaadinServlet.getCurrent().getServletContext().getRealPath("/images/seanau/");
		String targetFileName = baseIconFileName + "-" + count + ".png";
		iconBuilder.buildLogo(count.intValue(), new File(path), baseIconFileName + ".png", targetFileName);

		scheduleButton.setIcon(new ExternalResource("images/seanau/" + targetFileName));
		scheduleButton.setDescription("Schedule");
		scheduleButton.setWidth("50");
		scheduleButton.setHeight(buttonHeight);
		scheduleButton.addClickListener(new ClickEventLogged.ClickListener()
		{

			private static final long serialVersionUID = 7207441556779172217L;

			@Override
			public void clicked(ClickEvent event)
			{
				new JasperReportSchedulerWindow(reportProperties, builder.getReportParameters());
			}
		});
		buttonContainer.addComponent(scheduleButton);
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
		displayPanel = new BrowserFrame();
		displayPanel.setSizeFull();
		displayPanel.setStyleName("njadmin-hide-overflow-for-help");
		displayPanel.setImmediate(true);
		displayPanel.setId(PRINT_PANEL_ID);

		splitPanel.setSecondComponent(displayPanel);
		return displayPanel;

	}

	/**
	 * intended to allow the pdf to print as it opens.
	 *
	 * firefox prevents it, and for large pdfs in chrome it is firing too soon
	 * and prevents display of the pdf as a result
	 */
	protected void readyToPrint(final OutputFormat format)
	{

		// if (format == OutputFormat.PDF)
		// {
		// JavaScript.getCurrent().execute(
		// "window.document.getElementById('" + PRINT_PANEL_ID +
		// "').childNodes[0].contentWindow.focus()");
		// JavaScript.getCurrent().execute(
		// "window.document.getElementById('" + PRINT_PANEL_ID +
		// "').childNodes[0].contentWindow.print()");
		// }

	}

	private boolean cancelled;

	volatile boolean streamReady = false;
	volatile boolean streamConnected = false;

	private void generateReport(final JasperManager.OutputFormat outputFormat,
			final Collection<ReportParameter<?>> params)

	{
		streamConnected = false;
		streamReady = false;

		splitPanel.setSecondComponent(splash);

		manager = null;
		boolean validParams = true;
		// if there is a report chooser parameter then swap out the report
		// manager for the selected report.
		for (ReportParameter<?> p : params)
		{
			if (p instanceof ReportChooser)
			{
				ReportChooser chooser = (ReportChooser) p;
				manager = new JasperManager(chooser.getReportProperties(reportProperties));
				Preconditions.checkNotNull(manager, "chooser returned a NULL JasperManager.");
			}
			else
			{
				validParams &= p.validate();
			}
		}
		if (!validParams)
		{
			Notification.show("Please correct your filters and try again", Type.ERROR_MESSAGE);
			printButton.setEnabled(true);
			exportButton.setEnabled(true);
			showButton.setEnabled(true);
			for (ExpanderComponent componet : components)
			{
				componet.getComponent().setEnabled(true);
			}
			return;
		}
		if (manager == null)
		{
			this.manager = new JasperManager(reportProperties);
		}

		CancelListener cancelListener = getProgressDialogCancelListener();
		final WorkingDialog dialog = new WorkingDialog("Generating report, please be patient", "Please wait",
				cancelListener);
		dialog.setHeight("150");

		UI.getCurrent().addWindow(dialog);

		getProgressDialogRefreshListener(dialog);

		getStreamConnectorRefreshListener(outputFormat);

		JasperProgressListener listener = getJasperManagerProgressListener(UI.getCurrent(), dialog, outputFormat);
		manager.exportAsync(outputFormat, params, listener);

	}

	private JasperProgressListener getJasperManagerProgressListener(final UI ui, final WorkingDialog dialog,
			final OutputFormat outputFormat)
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

				dialog.close();
			}

			@Override
			public void completed()
			{
				ui.access(new Runnable()
				{
					@Override
					public void run()
					{
						reportFinished();
						JasperReportLayout.this.readyToPrint(outputFormat);
					}
				});

			}
		};
		return listener;
	}

	private void getStreamConnectorRefreshListener(final JasperManager.OutputFormat outputFormat)
	{
		UI.getCurrent().setPollInterval(500);
		UI.getCurrent().addPollListener(new PollListener()
		{

			private static final long serialVersionUID = -5641305025399715756L;

			@Override
			public void poll(PollEvent event)
			{

				if (streamReady && !streamConnected)
				{
					// jasper manager is ready, so get the report stream and set
					// it as the source for the display panel
					streamConnected = true;
					StreamResource resource = new StreamResource(getReportStream(), "report");
					resource.setMIMEType(outputFormat.getMimeType());
					resource.setCacheTime(-1);
					resource.setFilename(exportFileName(outputFormat));

					if (outputFormat == OutputFormat.CSV)
					{
						csv.setSource(resource);
						showCsvSplash();
					}
					else
					{
						getDisplayPanel().setSource(resource);
					}
					UI.getCurrent().removePollListener(this);

				}

			}

			private String exportFileName(final JasperManager.OutputFormat outputFormat)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				String name = reportProperties.getReportTitle() + "at" + sdf.format(new Date());
				for (ReportParameter<?> param : builder.getReportParameters())
				{
					if (param.showFilter())
					{
						name += "-" + param.getLabel();
						for (String parameterName : param.getParameterNames())
						{
							name += "-" + param.getDisplayValue(parameterName);

						}
					}
				}

				return name + outputFormat.getFileExtension();

			}
		});

	}

	private void showCsvSplash()
	{
		VerticalLayout csvSplash = new VerticalLayout();
		csvSplash.setMargin(true);

		Label titleLabel = new Label("<h1>" + reportProperties.getReportTitle() + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		csvSplash.addComponent(titleLabel);

		Label label = new Label("<font size='4' >Excel (CSV) download initiated.</font>");
		label.setContentMode(ContentMode.HTML);
		csvSplash.addComponent(label);

		splitPanel.setSecondComponent(csvSplash);

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

	private void getProgressDialogRefreshListener(final WorkingDialog dialog)
	{
		final Table reportQueue = new Table();
		reportQueue.addContainerProperty("Time", String.class, "");
		reportQueue.addContainerProperty("Report Name", String.class, "");
		reportQueue.addContainerProperty("User", String.class, "");
		reportQueue.addContainerProperty("Status", String.class, "");
		reportQueue.setSizeFull();
		reportQueue.setHeight("150");
		reportQueue.setWidth("100%");
		reportQueue.setColumnWidth("Time", 50);
		reportQueue.setColumnWidth("Report Name", 150);
		reportQueue.setColumnWidth("User", 100);
		// reportQueue.setColumnWidth("Status", 100);

		UI.getCurrent().setPollInterval(500);
		UI.getCurrent().addPollListener(new PollListener()
		{
			int refreshDivider = 0;
			boolean tableAdded = false;

			private static final long serialVersionUID = -5641305025399715756L;

			@SuppressWarnings("unchecked")
			@Override
			public void poll(PollEvent event)
			{

				if (manager == null)
				{
					return;
				}
				ReportStatus status = manager.getStatus();
				dialog.progress(0, 0, status.getStatus());
				if (refreshDivider % 4 == 0)
				{
					if (status.getEntries().size() > 0)
					{
						reportQueue.removeAllItems();

						for (QueueEntry entry : status.getEntries())
						{
							Object id = reportQueue.addItem();
							Item item = reportQueue.getItem(id);
							item.getItemProperty("Time").setValue(entry.getTime());
							item.getItemProperty("Report Name").setValue(entry.getReportName());
							item.getItemProperty("User").setValue(entry.getUser());
							item.getItemProperty("Status").setValue(entry.getStatus());
						}
						if (!tableAdded)
						{
							dialog.addUserComponent(reportQueue);
							tableAdded = true;
						}
						dialog.setWidth("600");

						dialog.setHeight("350");
						dialog.center();
					}
					else
					{
						dialog.removeUserComponent(reportQueue);
						dialog.setWidth("300");

						dialog.setHeight("150");
						dialog.center();
					}
				}
				if (cancelled)
				{
					printButton.setEnabled(true);
					exportButton.setEnabled(true);
					showButton.setEnabled(true);
					for (ExpanderComponent componet : components)
					{
						componet.getComponent().setEnabled(true);
					}
					UI.getCurrent().removePollListener(this);
					UI.getCurrent().setPollInterval(-1);
					dialog.close();
				}
				refreshDivider++;
			}
		});
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
				splitPanel.setSecondComponent(splash);
			}
		};
		return cancelListener;
	}

}
