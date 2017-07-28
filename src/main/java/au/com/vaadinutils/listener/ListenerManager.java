package au.com.vaadinutils.listener;

public interface ListenerManager<K>
{
	void addListener(K listener);

	public void removeListener(K listener);

	public void notifyListeners(ListenerCallback<K> callback);

	public boolean hasListeners();

	public void destroy();
}
