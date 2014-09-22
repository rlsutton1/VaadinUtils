package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class SecurityManagerFactoryProxy
{

	volatile static SecurityManagerFactory smf = new DefaultSecuritymanagerFactory();

	static public CrudSecurityManager getSecurityManager(Class<?> baseCrudView) 
	{
		return smf.buildSecurityManager(baseCrudView);
	}

	public static void setFactory(SecurityManagerFactory factory)
	{
		smf = factory;
	}
}
