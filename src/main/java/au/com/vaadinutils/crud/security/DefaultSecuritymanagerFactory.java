package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class DefaultSecuritymanagerFactory implements SecurityManagerFactory
{

	@Override
	public CrudSecurityManager buildSecurityManager(Object baseCrudView)
	{

		return new AllowAllSecurityManager();
	}

}
