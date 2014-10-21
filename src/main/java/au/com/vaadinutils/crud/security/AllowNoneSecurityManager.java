package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class AllowNoneSecurityManager implements CrudSecurityManager
{

	@Override
	public boolean canUserView()
	{

		return false;
	}

	@Override
	public boolean canUser(Enum<?> changeAccountGroups)
	{
		return false;
	}

	@Override
	public boolean canUserDelete()
	{
		return false;
	}

	@Override
	public boolean canUserEdit()
	{
		return false;
	}

	@Override
	public boolean canUserCreate()
	{
		return false;
	}

	@Override
	public Long getAccountId()
	{
		return -1l;
	}

	@Override
	public boolean isUserSuperUser()
	{
		return false;
	}

	@Override
	public String getFeatureName()
	{
		return "";
	}
}
