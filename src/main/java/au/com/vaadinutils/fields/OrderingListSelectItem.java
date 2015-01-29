package au.com.vaadinutils.fields;

public class OrderingListSelectItem
{
	private Long pk;
	private String caption;

	public OrderingListSelectItem(Long pk, String caption)
	{
		this.pk = pk;
		this.caption = caption;
	}

	public String toString()
	{
		return caption;
	}

	public Long getPk()
	{
		return pk;
	}

	public void setPk(Long pk)
	{
		this.pk = pk;
	}

	public String getCaption()
	{
		return caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

}
