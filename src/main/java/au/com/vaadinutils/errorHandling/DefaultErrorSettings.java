package au.com.vaadinutils.errorHandling;


public class DefaultErrorSettings implements ErrorSettings
{
	
	
	@Override
	public String getSystemName()
	{
		return "System name not set";
	}

	@Override
	public String getViewName()
	{
		return "View not know";
	}

	@Override
	public String getUserName()
	{
		return "User not known";
	}

	@Override
	public String getTargetEmailAddress()
	{
		return "Target email address not set";
	}

	@Override
	public void sendEmail(String emailAddress, String subject, String bodyText)
	{
		// TODO Auto-generated method stub
		
	}

	
}
