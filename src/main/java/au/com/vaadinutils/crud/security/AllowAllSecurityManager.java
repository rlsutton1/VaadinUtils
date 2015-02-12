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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canUserDelete()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canUserEdit()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canUserCreate()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Long getAccountId()
	{

		return -1l;
	}

	@Override
	public boolean isUserSuperUser()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getFeatureName()
	{
		// TODO Auto-generated method stub
		return "";
	}
}
