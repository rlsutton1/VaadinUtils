package au.com.vaadinutils.listener;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class GenericListenerManager<K> implements ListenerManager<K>
{
	private Map<K, Date> listeners = createSet();

	/**
	 * I considered weak references, but if you pass in an anonoumous listener
	 * it could be GC'd immediately
	 * 
	 * @return
	 */

	protected Map<K, Date> createSet()
	{
		return new LinkedHashMap<>();
	}

	private long maxSize;
	private long highWaterMark = 0;
	Logger logger = LogManager.getLogger();
	private String name;

	public GenericListenerManager(String name, long maxSize)
	{
		this.maxSize = maxSize;
		this.name = name;
	}

	@Override
	public void addListener(K listener)
	{

		listeners.put(listener, new Date());
		int size = listeners.size();
		if (size > (maxSize * 0.8) && highWaterMark < size)
		{
			highWaterMark = size;
			logger.warn("Listeners for '{}' have exceeded 50% of limit {}/{}", name, size, maxSize);
		}
		if (size > maxSize)
		{
			Iterator<Entry<K, Date>> itr = listeners.entrySet().iterator();
			Entry<K, Date> removed = itr.next();
			Exception ex = new Exception("Removing listener " + removed.getKey() + " " + removed.getValue());
			logger.error(ex, ex);
			itr.remove();
		}

	}

	@Override
	public void removeListener(K listener)
	{
		listeners.remove(listener);
	}

	/**
	 * This method is a bit expensive. If you have a high volume application,
	 * dont use this.
	 * 
	 * this allows a listener to call back and remove it self without causing a
	 * co-mod error
	 * 
	 * @param callback
	 */
	@Override
	public void notifyListeners(ListenerCallback<K> callback)
	{
		List<K> temp = new LinkedList<>();
		temp.addAll(listeners.keySet());
		for (K listener : temp)
		{
			callback.invoke(listener);
		}

	}

	@Override
	public boolean hasListeners()
	{
		return !listeners.isEmpty();
	}
}
