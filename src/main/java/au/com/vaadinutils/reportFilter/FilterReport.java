package au.com.vaadinutils.reportFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.sf.jasperreports.engine.JRException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudAction;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperManager.OutputFormat;
import au.com.vaadinutils.listener.ClickEventLogged;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

interface ComboBoxAction
{
	public void exec() throws Exception;
}

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

	private JasperManager manager;

	protected FilterReport(String title, String servletUrl, String reportFileName)
	{
		this.title = title;
		this.servletUrl = servletUrl;
		this.reportFileName = reportFileName;
	}

	protected FilterReport(String title, JasperManager manager)
	{
		this.title = title;
		this.servletUrl = null;
		this.reportFileName = manager.getReportName();
		this.manager = manager;
	}

	@Override
	public Component getOptionsPanel()
	{
		filters = getFilters();
		VerticalLayout layout = new VerticalLayout();
		layout.setHeight("100%");
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		Label titleLabel = new Label("<h1>" + title + "</h1>");
		titleLabel.setContentMode(ContentMode.HTML);
		layout.addComponent(titleLabel);

		VerticalLayout filterLayout = new VerticalLayout();

		if (filters != null)
		{
			for (ReportParameter filter : filters)
			{
				Component component = filter.getComponent();
				// some filters (such as constants) will not have a component to
				// display
				if (component != null)
				{

					filterLayout.addComponent(component);
					if (filter.shouldExpand())
					{
						filterLayout.setExpandRatio(component, 1);
					}
				}
			}
		}

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
						logger.error(e,e);
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
			generateReport(OutputFormat.HTML);
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
			generateReport(OutputFormat.PDF);

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
			generateReport(OutputFormat.CSV);

		}

		public String toString()
		{
			return "Export to CSV";
		}

	}

	protected abstract List<ReportParameter> getFilters();

	private void generateReport(JasperManager.OutputFormat outputFormat) throws JRException, IOException
	{
		if (this.manager != null)
		{
			showReport(this.manager.export(outputFormat));
		}
		else
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

			logger.debug(target);

			showReport(target);
		}
	}
}
