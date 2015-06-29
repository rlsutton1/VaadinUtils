package au.com.vaadinutils.errorHandling;

import java.io.ByteArrayOutputStream;


public class DefaultErrorSettings implements ErrorSettings
{
	
	@Override
	public String getSupportCompanyName()
	{
		return "Support company name not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
	}
	
	@Override
	public String getSystemName()
	{
		return "System name not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
	}

	@Override
	public String getViewName()
	{
		return "View not know, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
	}

	@Override
	public String getUserName()
	{
		return "User not known, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
	}

	@Override
	public String getTargetEmailAddress()
	{
		return "Target email address not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
	}

	@Override
	public void sendEmail(String emailAddress, String subject, String bodyText, ByteArrayOutputStream attachment, String filename, String MIMEType)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getBuildVersion()
	{
		return "Target email address not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
	}

	
}
