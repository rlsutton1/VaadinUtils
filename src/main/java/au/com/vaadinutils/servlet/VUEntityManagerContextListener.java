package au.com.vaadinutils.servlet;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import au.com.vaadinutils.dao.EntityManagerProvider;

/**
 * You need to hook this class as a servlet context listener in your web.xml
 *
 * You then need to create an EntityManagerFactory which the rest of the code
 * relies on.
 *
 * @author bsutton
 *
 */
public abstract class VUEntityManagerContextListener implements ServletContextListener
{
	private static EntityManagerFactory emf;

	@Override
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "There is only a single instance of the ContextListener")
	public void contextInitialized(ServletContextEvent event)
	{

		emf = getEntityManagerFactory();
		EntityManagerProvider.setEntityManagerFactory(emf);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		if (emf != null)
			emf.close();
	}

	/**
	 * Implement this method to provide a factory. e.g.
	 *
	 * return Persistence.createEntityManagerFactory("scoutmaster");
	 *
	 * @return an EntityManagerFactory
	 */
	abstract protected EntityManagerFactory getEntityManagerFactory();

}