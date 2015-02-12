package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class AllowNoneSecurityManager implements CrudSecurityManager
{

	@Override
	public boolean canUserView()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canUser(Enum<?> changeAccountGroups)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canUserDelete()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canUserEdit()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canUserCreate()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Long getAccountId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserSuperUser()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getFeatureName()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
