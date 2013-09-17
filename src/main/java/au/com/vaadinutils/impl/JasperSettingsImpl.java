package au.com.vaadinutils.impl;

import java.io.File;

import au.com.vaadinutils.jasper.JasperSettings;

/**
 * If you want to use the Jasper Wizard you need to implement this class.
 * @author bsutton
 *
 */
public class JasperSettingsImpl implements JasperSettings
{

	@Override
	public String getReportDir()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getDocumentBase()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getInitParameterRealPath(String paramName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSmtpFQDN()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSmtpPort()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAuthRequired()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUsername()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getUseSSL()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getBounceEmailAddress()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
