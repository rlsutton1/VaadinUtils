package au.com.vaadinutils.listener;

public class ListenerManagerFactory
{

	public static <K> ListenerManager<K> createListenerManager(String name, long maxSize)
	{
		return new GenericListenerManager<>(name, maxSize);
	}

	public static <K> ListenerManager<K> createThreadSafeListenerManager(String name, long maxSize)
	{
		return new GenericListenerManagerThreadSafe<>(name, maxSize);
	}
}
