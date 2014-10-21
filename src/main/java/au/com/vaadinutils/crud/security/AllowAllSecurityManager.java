package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

/**
 * allow all execept super user
 * 
 * @author rsutton
 * 
 */
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
