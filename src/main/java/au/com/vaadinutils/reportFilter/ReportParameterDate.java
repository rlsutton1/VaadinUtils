package au.com.vaadinutils.reportFilter;

import java.util.Date;

import org.joda.time.DateTime;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;

public class ReportParameterDate extends ReportParameter<Date>
{
	protected DateField field;

	public ReportParameterDate(String caption, String parameterName)
	{
		super( parameterName);
		field = new DateField(caption, new DateTime().withTimeAtStartOfDay().toDate());
		field.setResolution(Resolution.DAY);
		field.setDateFormat("yyyy/MM/dd");
	}

	@Override
	public Date getValue()
	{
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//return sdf.format(field.getValue());
		return field.getValue();
	}

	@Override
	public Component getComponent()
	{
		return field;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
	}

	@Override
	public void setDefaultValue(Date defaultValue)
	{
		this.field.setValue(defaultValue);
		
	}
}
