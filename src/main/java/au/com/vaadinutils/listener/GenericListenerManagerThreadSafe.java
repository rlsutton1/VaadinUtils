package au.com.vaadinutils.listener;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class GenericListenerManagerThreadSafe<K> extends GenericListenerManager<K>
{
	public GenericListenerManagerThreadSafe(String name, long maxSize)
	{
		super(name, maxSize);
	}

	@Override
	protected Map<K, Date> createSet()
	{
		return new ConcurrentHashMap<>();
	}

}
