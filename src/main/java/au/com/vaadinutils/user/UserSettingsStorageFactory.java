package au.com.vaadinutils.user;

public class UserSettingsStorageFactory
{
	// Logger logger = LogManager.getLogger();

	static UserSettingsStorage storage = new UserSettingsStorageNoOpImpl();

	static public UserSettingsStorage getUserSettingsStorage()
	{
		return storage;
	}

	static public void setStorageEngine(UserSettingsStorage storageEngine)
	{
		storage = storageEngine;
	}
}
