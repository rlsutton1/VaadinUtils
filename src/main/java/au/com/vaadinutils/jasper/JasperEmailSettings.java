package au.com.vaadinutils.jasper;


public interface JasperEmailSettings
{

	String getSmtpFQDN();

	Integer getSmtpPort();

	boolean isAuthRequired();

	String getUsername();

	String getPassword();

	boolean getUseSSL();

	String getBounceEmailAddress();

}
