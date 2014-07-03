package au.com.vaadinutils.crud.security;

import java.util.concurrent.ExecutionException;

import au.com.vaadinutils.crud.CrudSecurityManager;

public interface SecurityManagerFactory
{

	CrudSecurityManager buildSecurityManager(Object baseCrudView) throws ExecutionException;

}
