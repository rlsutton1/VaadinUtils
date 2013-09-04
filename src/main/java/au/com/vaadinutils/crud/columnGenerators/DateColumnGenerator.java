package au.com.vaadinutils.crud.columnGenerators;

import java.text.SimpleDateFormat;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

public class DateColumnGenerator implements ColumnGenerator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8975939959321307597L;
	private SimpleDateFormat sdf;
	private String propertyId;

	public DateColumnGenerator(String format, String propertyId)
	{
		sdf = new SimpleDateFormat(format);
		this.propertyId = propertyId;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId)
	{
		Object value = source.getItem(itemId).getItemProperty(propertyId).getValue();
		return new Label(sdf.format(value));
	}

}
