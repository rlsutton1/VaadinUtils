package au.com.vaadinutils.errorHandling;

public interface ErrorSettings
{

	String getSupportCompanyName();
	
	String getSystemName();

	String getViewName();

	String getUserName();

	String getTargetEmailAddress();

	public void sendEmail(String emailAddress, String subject, String bodyText);

}
