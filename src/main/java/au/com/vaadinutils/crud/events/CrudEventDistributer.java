package au.com.vaadinutils.crud.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import au.com.vaadinutils.audit.AuditFactory;
import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudEntity;

public enum CrudEventDistributer
{
	SELF;
	// Logger logger = LogManager.getLogger();

	Map<Class<? extends BaseCrudView<?>>, List<CrudEventListener>> listeners = new ConcurrentHashMap<>();

	public static synchronized void addListener(Class<? extends BaseCrudView<?>> type, CrudEventListener listener)
	{
		List<CrudEventListener> list = SELF.listeners.get(type);
		if (list == null)
		{
			list = new CopyOnWriteArrayList<CrudEventListener>();
			SELF.listeners.put(type, list);
		}
		list.add(listener);
	}

	public static void removeListener(CrudEventListener listener, Class<? extends BaseCrudView<?>> type)
	{
		List<CrudEventListener> list = SELF.listeners.get(type);
		if (list != null)
		{
			list.remove(listener);
		}

	}

	public static <T extends CrudEntity> void publishEvent(BaseCrudView<T> view, CrudEventType event, T entity)
	{
		List<CrudEventListener> interestedParties = SELF.listeners.get(view.getClass());
		if (interestedParties != null)
		{
			for (CrudEventListener listener : interestedParties)
			{
				listener.crudEvent(event, entity);
			}
		}
		
		AuditFactory.getAuditor().audit(event, entity);
	}
}
