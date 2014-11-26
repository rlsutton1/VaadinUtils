package au.com.vaadinutils.audit;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.events.CrudEventType;

public interface Auditor
{

	void audit(CrudEventType event, CrudEntity entity);

}
