package au.com.vaadinutils.impl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * You need to hook this class as a servlet context listener in your web.xml
 * 
 * You then need to create an EntityManagerFactory which the rest of the code relies on.
 * 
 * @author bsutton
 *
 */
@WebListener  
public class LocalEntityManagerFactory implements ServletContextListener
{
	private static EntityManagerFactory emf;

	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		// TODO replace this exception with something like:
		//emf = Persistence.createEntityManagerFactory("scoutmaster");
		throw new IllegalStateException("You need to implement this method!");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		emf.close();
	}

	public static EntityManager createEntityManager()
	{
		if (emf == null)
		{
			throw new IllegalStateException("Context is not initialized yet.");
		}

		return emf.createEntityManager();
	}
}