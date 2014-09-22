package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public interface SecurityManagerFactory
{

	CrudSecurityManager buildSecurityManager(Class<?> baseCrudView) ;

}
