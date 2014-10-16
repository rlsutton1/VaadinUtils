package au.com.vaadinutils.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.ui.UI;

public abstract class EntityRunnable implements Runnable
{

	final private UI ui;
	Logger logger = LogManager.getLogger();



	public EntityRunnable(UI ui)
	{
		this.ui  =ui;
	}

	@Override
	public void run()
	{
		try
		{
			EntityManagerProvider.setThreadLocalEntityManager(new EntityWorker<Void>()
			{

				@Override
				public Void exec() throws Exception
				{
					run(ui);
					return null;
				}

			});
		}
		catch (Exception e)
		{
			
			logger.error(e,e);
		}

	}

	public abstract void run(UI ui);

}
