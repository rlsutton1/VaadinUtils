package au.com.vaadinutils.errorHandling;

import java.io.ByteArrayOutputStream;

public interface ErrorSettings
{

	String getSupportCompanyName();
	
	String getSystemName();

	String getViewName();

	String getUserName();

	String getTargetEmailAddress();

	public void sendEmail(String emailAddress, String subject, String bodyText, ByteArrayOutputStream attachment, String filename, String MIMEType);

	String getBuildVersion();

	String getUserEmail();

}
