package au.com.vaadinutils.filters;

import javax.persistence.EntityManager;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.Transaction;
import au.com.vaadinutils.impl.LocalEntityManagerFactory;

/**
 * Designed to inject the EntityManager into requests that arrive via websockets (Vaadin Push) as these
 * do not go through the standard servlet filter mechanism.
 * 
 * This class is installed by adding a parameter to the VaadinServlet mapping in web.xml.
 * 
 */

public class AtmosphereFilter extends AtmosphereInterceptorAdapter
{
	Transaction t;

	public Action inspect(AtmosphereResource r)
	{
		// do pre-request stuff

		EntityManager em = LocalEntityManagerFactory.createEntityManager();
		t = new Transaction(em);

		// Create and set the entity manager
		EntityManagerProvider.INSTANCE.setCurrentEntityManager(em);

		return super.inspect(r);
	}

	// do post-request stuff (Vaadin request handling is done at this point)
	public void postInspect(AtmosphereResource r)
	{
		try
		{
			t.commit();
		}
		catch (Throwable e)
		{
			// Reset the entity manager
			EntityManagerProvider.INSTANCE.setCurrentEntityManager(null);
			throw new RuntimeException(e);
		}


	}
}
