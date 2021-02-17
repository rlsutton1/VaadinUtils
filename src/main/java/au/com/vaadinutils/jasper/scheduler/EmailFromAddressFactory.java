package au.com.vaadinutils.jasper.scheduler;

public class EmailFromAddressFactory
{

	static String emailAddress;

	public static void setFromEmailAddress(String fromEmailAddress)
	{
		emailAddress = fromEmailAddress;
	}

	public static String getFromEmailAddress()
	{
		if (emailAddress == null)
		{
			throw new RuntimeException("EmailFromAddressFactory.setFromEmailAddress() "
					+ "must be called before getFromEmailAddress() is called.");
		}
		return emailAddress;
	}
}
