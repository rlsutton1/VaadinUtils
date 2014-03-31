package au.com.vaadinutils.jasper.ui;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.jasper.filter.ReportFilterUIBuilder;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.parameter.ReportParameterConstant;
import au.com.vaadinutils.listener.CancelListener;
import au.com.vaadinutils.listener.ClickEventLogged;
import au.com.vaadinutils.listener.CompleteListener;
import au.com.vaadinutils.ui.WorkingDialog;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
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
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;
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

	String title;

	JasperManager manager;
	private ReportFilterUIBuilder builder;

	private JasperReportDataProvider dataProvider;

	private List<Component> components;

	private NativeButton showButton;

	private NativeButton printButton;

	private NativeButton exportButton;

	private VerticalLayout splash;

	protected JasperReportLayout(String title, JasperManager manager, JasperReportDataProvider dataProvider)
	{
		this.title = title;
		this.manager = manager;
		this.builder = dataProvider.getFilterBuilder(manager);
		this.dataProvider = dataProvider;
	}

	protected void initScreen()
	{
		this.setSizeFull();

		this.setSplitPosition(20);
		this.setFirstComponent(getOptionsPanel());
		if (!builder.hasFilters())
		{
			setSplitPosition(10);
		}

		splash = new VerticalLayout();
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
				generateReport(dataProvider.getDefaultFormat(), JasperReportLayout.this.builder.getReportParameters());
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
							subReportFileName,title, manager.getSettings());

					List<ReportParameter<?>> subFilters = new LinkedList<ReportParameter<?>>();

					@SuppressWarnings("unchecked")
					Iterator<String> itr = params.keys();
					while (itr.hasNext())
					{
						String key = itr.next();
						subFilters.add(new ReportParameterConstant(key, params.getString(key)));
					}

					new JasperReportPopUp(subTitle, subManager, subFilters, dataProvider);
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
		layout.setHeight("100%");
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
		exportButton.setDescription("Export (CSV)");
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
			Label filterLabel = new Label("<b>Filters</b>");
			filterLabel.setContentMode(ContentMode.HTML);
			filterPanel.addComponent(filterLabel);

			for (Component componet : components)
			{
				filterPanel.addComponent(componet);
			}
			layout.addComponent(filterPanel);
			layout.setExpandRatio(filterPanel, 1.0f);
		}

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
					for (Component componet : components)
					{
						componet.setEnabled(false);
					}

					generateReport(format, JasperReportLayout.this.builder.getReportParameters());

					
				}
				catch (Exception e)
				{
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

	protected void generateReport(final JasperManager.OutputFormat outputFormat,
			final Collection<ReportParameter<?>> params)

	{

		CancelListener cancelListener = getProgressDialogCancelListener();
		final WorkingDialog dialog = new WorkingDialog("Generating report", "Please wait", cancelListener );
		
		
		
		UI.getCurrent().addWindow(dialog);
		final Refresher refresher = new Refresher();
		RefreshListener refreshListener = getProgressDialogRefreshListener(dialog, refresher);
		refresher.addListener(refreshListener);
		refresher.setRefreshInterval(200);
		addExtension(refresher);
		
		StreamSource source = getReportStream(outputFormat, params, dialog, refresher);
		StreamResource resource = new StreamResource(source, "report");
		resource.setMIMEType(outputFormat.getMimeType());
		resource.setCacheTime(-1);
		resource.setFilename("jr-"+System.currentTimeMillis());
		getDisplayPanel().setSource(resource);
		

	}

	private StreamSource getReportStream(final JasperManager.OutputFormat outputFormat,
			final Collection<ReportParameter<?>> params, final WorkingDialog dialog, final Refresher refresher)
	{
		@SuppressWarnings("serial")
		StreamSource source = new StreamSource()
		{

			@Override
			public InputStream getStream()
			{
				try
				{
					CompleteListener completeListener = new CompleteListener()
					{

						@Override
						public void complete()
						{
							printButton.setEnabled(true);
							exportButton.setEnabled(true);
							showButton.setEnabled(true);
							for (Component componet : components)
							{
								componet.setEnabled(true);
							}
							removeExtension(refresher);
							dialog.close();
						}
					};
					return manager.exportAsync(dataProvider, outputFormat, params, completeListener);
				}
				catch (InterruptedException e)
				{
					logger.error(e, e);
				}
				catch (RuntimeException e)
				{
					Notification.show(e.getMessage(),Type.ERROR_MESSAGE);
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
				dialog.progress(0,0,manager.getStatus());
				if (cancelled)
				{
					printButton.setEnabled(true);
					exportButton.setEnabled(true);
					showButton.setEnabled(true);
					for (Component componet : components)
					{
						componet.setEnabled(true);
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
