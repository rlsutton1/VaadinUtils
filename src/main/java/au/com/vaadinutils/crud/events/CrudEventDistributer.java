package au.com.vaadinutils.crud.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import au.com.vaadinutils.audit.AuditFactory;
import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.listener.ListenerCallback;
import au.com.vaadinutils.listener.ListenerManager;
import au.com.vaadinutils.listener.ListenerManagerFactory;

public enum CrudEventDistributer
{
	SELF;
	// Logger logger = LogManager.getLogger();

	Map<Class<? extends BaseCrudView<?>>, ListenerManager<CrudEventListener>> listeners = new ConcurrentHashMap<>();

	public static synchronized void addListener(Class<? extends BaseCrudView<?>> type, CrudEventListener listener)
	{
		ListenerManager<CrudEventListener> list = SELF.listeners.get(type);
		if (list == null)
		{
			list = ListenerManagerFactory.createThreadSafeListenerManager("CrudEventDistributer", 200);

			SELF.listeners.put(type, list);
		}
		list.addListener(listener);
	}

	public static void removeListener(Class<? extends BaseCrudView<?>> type, CrudEventListener listener)
	{
		ListenerManager<CrudEventListener> list = SELF.listeners.get(type);
		if (list != null)
		{
			list.removeListener(listener);
		}

	}

	public static <T extends CrudEntity> void publishEvent(BaseCrudView<T> view, final CrudEventType event,
			final T entity)
	{
		ListenerManager<CrudEventListener> interestedParties = SELF.listeners.get(view.getClass());
		if (interestedParties != null)
		{
			interestedParties.notifyListeners(new ListenerCallback<CrudEventListener>()
			{

				@Override
				public void invoke(CrudEventListener listener)
				{
					listener.crudEvent(event, entity);
				}
			});

		}

		AuditFactory.getAuditor().audit(event, entity);
	}
}
