package au.org.scoutmaster.jasper;

import java.io.File;

public interface JasperSettings
{

	String getReportDir();

	File getDocumentBase();

	File getInitParameterRealPath(String paramName);

	String getSmtpFQDN();

	Integer getSmtpPort();

	boolean isAuthRequired();

	String getUsername();

	String getPassword();

	boolean getUseSSL();

	String getBounceEmailAddress();

}
