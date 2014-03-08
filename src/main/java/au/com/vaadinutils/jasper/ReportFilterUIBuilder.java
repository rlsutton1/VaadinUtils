package au.com.vaadinutils.jasper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import net.sf.jasperreports.engine.JRParameter;
import au.com.vaadinutils.reportFilter.ReportParameter;
import au.com.vaadinutils.reportFilter.ReportParameterDate;
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
	private List<ReportParameter<?>> rparams = new ArrayList<ReportParameter<?>>();

	public ReportFilterUIBuilder(JasperManager manager)
	{
		this.manager = manager;
	}

	public ReportFilterFieldBuilder addField(String label, String parameterName)
	{
		Preconditions.checkArgument(this.manager.paramExists(parameterName), "The passed Jasper Report parameter: "
				+ parameterName + " does not existing on the Report");

		JRParameter param = manager.getParameter(parameterName);

		buildFieldForParamType(label, param);
		return this;
	}
	
	public ReportFilterDateFieldBuilder addDateField(String label, String parameterName)
	{
		addField(label, parameterName);
		
		return this;
	}

	@Override
	public void setDate(DateTime date)
	{
		@SuppressWarnings("unchecked")
		ReportParameter<Date> param = (ReportParameter<Date>) rparams.get(rparams.size() - 1);
		param.setDefaultValue(date.toDate());
		
	}


	public AbstractLayout buildLayout()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		if (hasFilters())
		{
			Label filterLabel = new Label("<b>Filters</b>");
			filterLabel.setContentMode(ContentMode.HTML);
			layout.addComponent(filterLabel);
			

			for (ReportParameter<?> rparam : rparams)
			{
				layout.addComponent(rparam.getComponent());
			}
		}
		return layout;
	}

	private void buildFieldForParamType(String label, JRParameter param)
	{
		ReportParameter<?> rparam = null;

		String className = param.getValueClassName();
		if (className.equals("java.util.Date"))
		{

			rparam = new ReportParameterDate(label, param.getName());
		}
		else if (className.equals("java.util.String"))
		{
			rparam = new ReportParameterString(label, param.getName());
		}

		if (rparam == null)
			throw new IllegalArgumentException("FIXME: The jasper parameter type " + className + " is not supported.");

		rparams.add(rparam);

	}

	public List<ReportParameter<?>> getReportParameters()
	{
		return rparams;
	}

	/**
	 * Return true if the builder has any filters defined.
	 */
	public boolean hasFilters()
	{
		return this.rparams.size() > 0;
	}

	
}
