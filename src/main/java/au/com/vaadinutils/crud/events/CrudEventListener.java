package au.com.vaadinutils.crud.events;

import au.com.vaadinutils.crud.CrudEntity;

public interface CrudEventListener
{
	// Logger logger = LogManager.getLogger();
	
	public void crudEvent(CrudEventType event,CrudEntity entity);
}
