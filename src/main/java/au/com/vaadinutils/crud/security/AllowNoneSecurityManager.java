package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class AllowNoneSecurityManager implements CrudSecurityManager
{

	@Override
	public boolean canUseView()
	{

		return false;
	}
}
