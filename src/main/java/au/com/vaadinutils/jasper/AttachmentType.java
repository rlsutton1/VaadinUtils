package au.com.vaadinutils.jasper;

public enum AttachmentType
{
	PDF("application/pdf"), CSV("application/csv"), HTML("text/html");

	private String type;

	AttachmentType(String type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return type;
	}
}