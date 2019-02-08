package au.com.vaadinutils.jasper;

import java.net.URISyntaxException;

public interface JasperEmailSettings
{

	String getSmtpFQDN() throws URISyntaxException;

	Integer getSmtpPort() throws URISyntaxException;

	boolean isAuthRequired() throws URISyntaxException;

	String getUsername() throws URISyntaxException;

	String getPassword() throws URISyntaxException;

	boolean getUseSSL() throws URISyntaxException;

	String getBounceEmailAddress() throws URISyntaxException;

}
