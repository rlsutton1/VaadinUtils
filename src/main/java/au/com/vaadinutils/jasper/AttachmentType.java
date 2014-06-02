package au.com.vaadinutils.jasper;

public enum AttachmentType
{
	PDF("application/pdf",".pdf"), CSV("application/csv",".csv"), HTML("text/html",".html");

	private String type;
	private String extension;

	AttachmentType(String type,String fileExtension)
	{
		this.type = type;
		extension = fileExtension;
	}

	@Override
	public String toString()
	{
		return type;
	}
	
	public String getFileExtension()
	{
		return extension;
	}
}