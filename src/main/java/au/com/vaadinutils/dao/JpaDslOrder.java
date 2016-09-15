package au.com.vaadinutils.dao;

public class JpaDslOrder
{
	private String field = "";
	private boolean ascending = true;

	public JpaDslOrder(final String field, final boolean ascending)
	{
		this.field = field;
		this.ascending = ascending;
	}

	public String getField()
	{
		return field;
	}

	public boolean getAscending()
	{
		return ascending;
	}
}
