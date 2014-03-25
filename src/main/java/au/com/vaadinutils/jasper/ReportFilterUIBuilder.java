package au.com.vaadinutils.jasper;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import net.sf.jasperreports.engine.JRParameter;

import org.joda.time.DateTime;

import au.com.vaadinutils.reportFilter.ReportParameter;
import au.com.vaadinutils.reportFilter.ReportParameterDate;
import au.com.vaadinutils.reportFilter.ReportParameterEnum;
import au.com.vaadinutils.reportFilter.ReportParameterString;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class is used to build report filter UI by defining how report
 * parameters are to be displayed.
 * 
 * The builder also validates that all report parameters have been used and that
 * those used are defined by the report.
 * 
 * @author bsutton
 * 
 */
public class ReportFilterUIBuilder implements ReportFilterFieldBuilder, ReportFilterDateFieldBuilder
{
	private JasperManager manager;
	private LinkedList<ReportParameter<?>> rparams = new LinkedList<ReportParameter<?>>();

	public ReportFilterUIBuilder(JasperManager manager)
	{
		this.manager = manager;
	}

	@Override
	public ReportFilterFieldBuilder addTextField(String label, String parameterName)
	{
		ReportParameterString param = new ReportParameterString(label, parameterName);
		addField(param);

		return this;
	}

	@Override
	public ReportFilterDateFieldBuilder addDateField(String label, String parameterName)
	{
		ReportParameterDate param = new ReportParameterDate(label, parameterName);
		addField(param);

		return this;
	}

	@Override
	public <T extends Enum<?>> ReportFilterFieldBuilder addEnumField(String label, String paramName,
			Class<T> enumClass, T defaultValue)
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ReportParameterEnum<?> param = new ReportParameterEnum(label, defaultValue, paramName, enumClass);
		addField(param);

		return this;
	}

	@Override
	public ReportFilterFieldBuilder addField(ReportParameter<?> param)
	{
		String parameterName = param.getParameterName();

		// specific work around for prefixed report parameters
		if (parameterName.startsWith("ReportParameter"))
		{
			parameterName = parameterName.substring("ReportParameter".length(), parameterName.length());
		}
		Preconditions.checkArgument(this.manager.paramExists(parameterName), "The passed Jasper Report parameter: "
				+ parameterName + " does not exist in the Report " + manager.getReportFilename()
				+ ", valid parameters are " + getParameterList());
		JRParameter jrParam = manager.getParameter(parameterName);

		String expectedClass = param.getExpectedParameterClassName();
		Preconditions.checkArgument(expectedClass == null || jrParam.getValueClassName().equals(expectedClass),
				"Expected " + expectedClass + " but the ReportParameter type is " + jrParam.getValueClassName());

		rparams.add(param);

		return this;
	}

	private String getParameterList()
	{
		String params = "\n";
		for (JRParameter param : manager.getParameters())
		{
			params += param.getName() + "(" + param.getNestedTypeName() + ") \n";
		}
		return params;
	}

	@Override
	public ReportFilterDateFieldBuilder setDate(DateTime date)
	{
		@SuppressWarnings("unchecked")
		ReportParameter<Date> param = (ReportParameter<Date>) rparams.getLast();
		param.setDefaultValue(date.toDate());
		return this;

	}

	@Override
	public AbstractLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		// layout.setSpacing(true);
		// layout.setSizeFull();

		if (hasFilters())
		{
			Label filterLabel = new Label("<b>Filters</b>");
			filterLabel.setContentMode(ContentMode.HTML);
			layout.addComponent(filterLabel);

			for (ReportParameter<?> rparam : rparams)
			{
				if (rparam.showFilter())
				{
					layout.addComponent(rparam.getComponent());
				}
			}
		}
		return layout;
	}

	public Collection<ReportParameter<?>> getReportParameters()
	{
		return rparams;
	}

	/**
	 * Return true if the builder has any filters defined.
	 */
	public boolean hasFilters()
	{
		boolean ret = false;
		for (ReportParameter<?> param : rparams)
		{
			if (param.showFilter())
			{
				ret = true;
				break;
			}
		}
		return ret;
	}

}
