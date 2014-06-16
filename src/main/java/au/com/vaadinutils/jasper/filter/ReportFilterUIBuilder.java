package au.com.vaadinutils.jasper.filter;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.parameter.ReportParameterDateTimeRange;
import au.com.vaadinutils.jasper.parameter.ReportParameterEnum;
import au.com.vaadinutils.jasper.parameter.ReportParameterString;

import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.TabSheet.Tab;
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
	private Set<ReportParameter<?>> rparams = new LinkedHashSet<ReportParameter<?>>();
	ReportParameter<?> lastAdded = null;

	public ReportFilterUIBuilder()
	{
	}

	@Override
	public ReportFilterFieldBuilder addTextField(String label, String parameterName)
	{
		ReportParameterString param = new ReportParameterString(label, parameterName);
		addField(param);

		return this;
	}

	@Override
	public ReportFilterDateFieldBuilder addDateField(String label, String startParameterName, String endParameterName)
	{
		ReportParameterDateTimeRange param = new ReportParameterDateTimeRange(label, startParameterName,
				endParameterName);
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
		rparams.add(param);
		lastAdded = param;

		return this;
	}

	@Override
	public ReportFilterDateFieldBuilder setDateRange(DateTime startDate, DateTime endDate)
	{
		@SuppressWarnings("unchecked")
		ReportParameter<Date> param = (ReportParameter<Date>) lastAdded;
		param.setStartDate(startDate.toDate());
		param.setEndDate(endDate.toDate());
		return this;

	}

	@Override
	public List<ExpanderComponent> buildLayout(Boolean hideDateFields)
	{
		List<ExpanderComponent> components = new LinkedList<ExpanderComponent>();

		boolean hasExpandingComponents = false;
		Accordion accordian = null;
		if (hasFilters())
		{

			for (ReportParameter<?> rparam : rparams)
			{
				if (rparam.showFilter())
				{
					// check if we should hide date fields, used for the
					// scheduler
					if (!hideDateFields || !rparam.isDateField())
					{
						if (rparam.shouldExpand())
						{
							if (accordian == null)
							{
								accordian = new Accordion();
								accordian.setSizeFull();
							}
							final Tab tab = accordian.addTab(rparam.getComponent(), rparam.getLabel());
							rparam.addValidateListener(new ValidateListener()
							{

								@Override
								public void setComponentError(ErrorMessage componentError)
								{
									tab.setComponentError(componentError);
								}
							});
							rparam.validate();

						}
						else
						{
							components.add(new ExpanderComponent(rparam.getComponent(), rparam.shouldExpand()));
						}
					}
				}
			}
		}
		if (accordian != null)
		{
			hasExpandingComponents = true;
			components.add(new ExpanderComponent(accordian, true));
		}

		if (!hasExpandingComponents)
		{
			// there are no expanding components, so add an empty expanding
			// component so the fields will group together at the top
			components.add(new ExpanderComponent(new VerticalLayout(), true));
		}
		
		// add 15px high layout to pack up the bottom of the layout, otherwise on
		// some sets of filters the last component is hidden
		VerticalLayout spacer = new VerticalLayout();
		spacer.setHeight("15");
		components.add(new ExpanderComponent(spacer,false));

		return components;
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
