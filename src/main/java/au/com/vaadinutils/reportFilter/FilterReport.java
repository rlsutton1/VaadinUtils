package au.com.vaadinutils.reportFilter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.listener.ClickEventLogged;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public abstract class FilterReport extends ReportView
{
	Logger logger = LogManager.getLogger(FilterReport.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5225827773450545086L;
	private List<ReportParameter> filters;
	private String title;
	private String servletUrl;
	private String reportFileName;

	protected FilterReport(String title, String servletUrl, String reportFileName)
	{
		this.title = title;
		this.servletUrl = servletUrl;
		this.reportFileName = reportFileName;
	}

	@Override
	public Component getOptionsPanel()
	{
		filters = getFilters();
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		layout.addComponent(new Label(title));

		for (ReportParameter filter : filters)
		{
			Component component = filter.getComponent();
			// some filters (such as constants) will not have a component to
			// display
			if (component != null)
			{

				layout.addComponent(component);
				if (filter.shouldExpand())
				{
					layout.setExpandRatio(component, 1);
				}
			}
		}

		Button startButton = new Button("Show");
		startButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = -1313098616695130664L;

			@Override
			public void clicked(ClickEvent event)
			{
				try
				{

					generateReport(OutputFormat.HTML);
				}
				catch (UnsupportedEncodingException e)
				{
					logger.error(e,e);
				}

			}

		});

		layout.addComponent(startButton);

		Button pdfButton = new Button("Print Quality");
		pdfButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = -1313098616695130664L;

			@Override
			public void clicked(ClickEvent event)
			{
				try
				{

					generateReport(OutputFormat.PDF);
				}
				catch (UnsupportedEncodingException e)
				{
					logger.error(e,e);
				}

			}

		});

		layout.addComponent(pdfButton);

		Button csvButton = new Button("Export CSV");
		csvButton.addClickListener(new ClickEventLogged.ClickListener()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = -1313098616695130664L;

			@Override
			public void clicked(ClickEvent event)
			{
				try
				{

					generateReport(OutputFormat.CSV);
				}
				catch (UnsupportedEncodingException e)
				{
					logger.error(e,e);
				}

			}

		});

		layout.addComponent(csvButton);

		return layout;
	}

	protected abstract List<ReportParameter> getFilters();

	enum OutputFormat
	{
		PDF, CSV, HTML;
	}

	private void generateReport(OutputFormat outputFormat) throws UnsupportedEncodingException
	{
		String target = servletUrl;
		target += "?OutputFormat=" + outputFormat.toString();
		target += "&ReportName=" + java.net.URLEncoder.encode(reportFileName, "UTF-8");
		target += "&ReportTitle=" + java.net.URLEncoder.encode(title, "UTF-8");
		target += "&uniqueifier=" + System.currentTimeMillis();
		for (ReportParameter filter : filters)
		{
			target += "&" + filter.getUrlEncodedKeyAndParameter();
		}

		System.out.println(target);

		showReport(target);
	}
}
