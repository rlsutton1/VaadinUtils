package au.com.vaadinutils.audit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.events.CrudEventType;

public class AuditorLoggingImpl implements Auditor
{
	Logger logger = LogManager.getLogger();

	@Override
	public void audit(CrudEventType event, CrudEntity entity)
	{
		logger.info("{} {} {} {}",event.toString(), entity.getClass().getSimpleName(), entity.getName() ,entity.getId());

	}
	// Logger logger = LogManager.getLogger();
}
