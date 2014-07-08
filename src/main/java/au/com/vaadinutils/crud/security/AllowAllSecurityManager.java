package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class AllowAllSecurityManager implements CrudSecurityManager
{

	@Override
	public boolean canUserView()
	{

		return true;
	}

	@Override
	public boolean canUser(Enum<?> changeAccountGroups)
	{
		
		return true;
	}

	@Override
	public boolean canUserDelete()
	{
		return true;
	}

	@Override
	public boolean canUserEdit()
	{
		return true;
	}

	@Override
	public boolean canUserCreate()
	{
		return true;
	}

	@Override
	public Long getAccountId()
	{
		return -1l;
	}
}
