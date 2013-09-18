package au.com.vaadinutils.servlet;

import javax.persistence.EntityManager;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.Transaction;

/**
 * Designed to inject the EntityManager into requests that arrive via websockets (Vaadin Push) as these
 * do not go through the standard servlet filter mechanism.
 * 
 * This class is installed by adding a parameter to the VaadinServlet mapping in web.xml.
 * 
 * e.g.
 * 	<servlet>
		<servlet-name>Vaadin Application Servlet</servlet-name>
		<servlet-class>com.vaadin.server.VaadinServlet</servlet-class>
		<init-param>
			<description>Vaadin UI to display</description>
			<param-name>UI</param-name>
			<param-value>au.org.scoutmaster.application.NavigatorUI</param-value>
		</init-param>
		<init-param>
			<param-name>org.atmosphere.cpr.AtmosphereInterceptor</param-name>
			<!-- comma-separated list of fully-qualified class names -->
			<param-value>au.com.vaadinutils.servlet.AtmosphereFilter</param-value>
		</init-param>
		<async-supported>true</async-supported>
	</servlet>

 * 
 */

public class AtmosphereFilter extends AtmosphereInterceptorAdapter
{
	Transaction t;

	public Action inspect(AtmosphereResource r)
	{
		// do pre-request stuff

		EntityManager em = EntityManagerProvider.INSTANCE.createEntityManager();
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
