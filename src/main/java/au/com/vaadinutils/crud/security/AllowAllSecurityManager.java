package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class AllowAllSecurityManager implements CrudSecurityManager
{

	@Override
	public boolean canUseView()
	{

		return true;
	}
}
