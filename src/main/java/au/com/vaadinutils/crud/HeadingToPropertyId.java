package au.com.vaadinutils.crud;

import com.vaadin.ui.Table.ColumnGenerator;

public class HeadingToPropertyId
{
	private String heading;
	private String propertyId;
	private ColumnGenerator columnGenerator;

	/**
	 * 
	 * @param heading
	 * @param propertyId
	 *            - the real field name
	 */
	public HeadingToPropertyId(String heading, String propertyId)
	{
		this.heading = heading;
		this.propertyId = propertyId;
		this.columnGenerator = null;
	}

	/**
	 * 
	 * @param heading
	 * @param propertyId
	 *            - the real field name
	 * @param columnGenerator2 
	 */
	public HeadingToPropertyId(String heading, String propertyId, ColumnGenerator columnGenerator2)
	{
		this.heading = heading;
		this.propertyId = propertyId;
		this.columnGenerator = columnGenerator2;
	}

	public String getPropertyId()
	{
		// TODO Auto-generated method stub
		return propertyId;
	}

	public String getHeader()
	{
		return heading;
	}

	public ColumnGenerator getColumnGenerator()
	{

		return columnGenerator;
	}

	public boolean isGenerated()
	{
		return columnGenerator != null;
	}

}
