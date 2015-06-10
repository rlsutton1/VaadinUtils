package au.com.vaadinutils.errorHandling;

import java.util.concurrent.atomic.AtomicReference;

public class ErrorSettingsFactory
{
	final static private AtomicReference<ErrorSettings> errorSettings = new AtomicReference<>();

	public static void setErrorSettings(ErrorSettings settings)
	{
		errorSettings.set(settings);
	}

	static ErrorSettings getErrorSettings()
	{

		errorSettings.compareAndSet(null, new DefaultErrorSettings());

		return errorSettings.get();
	}
}
