package au.com.vaadinutils.user;

public class UserSettingsStorageNoOpImpl implements UserSettingsStorage
{

	@Override
	public void store(String key, String value)
	{
	}

	@Override
	public String get(String string)
	{
		return "";
	}
}
