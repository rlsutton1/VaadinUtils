package au.com.vaadinutils.user;

public interface UserSettingsStorage
{

	void store(String key, String value);

	String get(String string);

}
