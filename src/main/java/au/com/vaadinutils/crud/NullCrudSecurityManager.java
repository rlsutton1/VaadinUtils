package au.com.vaadinutils.crud;

public class NullCrudSecurityManager implements CrudSecurityManager
{

	@Override
	public boolean canUseView()
	{

		return true;
	}
}
