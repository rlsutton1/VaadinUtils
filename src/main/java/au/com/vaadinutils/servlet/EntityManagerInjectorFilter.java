package au.com.vaadinutils.servlet;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.validation.ConstraintViolationException;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.Transaction;
import au.com.vaadinutils.errorHandling.ErrorWindow;

public class EntityManagerInjectorFilter implements Filter
{
	//private static  transient Logger logger   =  LogManager.getLogger(EntityManagerInjectorFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException
	{
		EntityManager em = EntityManagerProvider.createEntityManager();

		Transaction t = new Transaction(em);
		try
		{
			// Create and set the entity manager
			EntityManagerProvider.setCurrentEntityManager(em);
			

			// Handle the request
			filterChain.doFilter(servletRequest, servletResponse);

			t.commit();
		}
		catch (ConstraintViolationException e)
		{
			ErrorWindow.showErrorWindow(e);
			throw e;
		}
		finally
		{

			t.close();
			
			// Reset the entity manager
			EntityManagerProvider.setCurrentEntityManager(null);
			em.close();
		}
	}

	@Override
	public void destroy()
	{
		// entityManagerFactory = null;
	}
}
