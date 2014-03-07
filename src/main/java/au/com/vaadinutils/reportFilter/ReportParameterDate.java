package au.com.vaadinutils.reportFilter;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;

public class ReportParameterDate extends ReportParameter
{

	protected DateField field;

	public ReportParameterDate(String caption, String parameterName)
	{
		super( parameterName);
		field = new DateField(null, new DateTime().withTimeAtStartOfDay().toDate());
		field.setResolution(Resolution.SECOND);
		field.setDateFormat("yyyy/MM/dd HH:mm:ss");
		field.setCaption(caption);
	}

	@Override
	protected String getValue()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return sdf.format(field.getValue());
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

}
