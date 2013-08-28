package au.com.vaadinutils.crud;

public class HeadingToPropertyId
{
	private String heading;
	private String propertyId;

	/**
	 * 
	 * @param heading
	 * @param propertyId - the real field name
	 */
	public HeadingToPropertyId(String heading, String propertyId)
	{
		this.heading = heading;
		this.propertyId = propertyId;
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

}
