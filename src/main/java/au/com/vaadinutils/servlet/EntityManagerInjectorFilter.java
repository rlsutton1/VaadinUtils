package au.com.vaadinutils.servlet;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.Transaction;

public class EntityManagerInjectorFilter implements Filter
{
	private static  transient Logger logger   =  LogManager.getLogger(EntityManagerInjectorFilter.class);

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
		
		
		catch (ConstraintViolationException |RollbackException e)
		{
			Throwable ex = e;
			int i= 0;
			while (i < 5 && ex !=null && !(ex instanceof ConstraintViolationException))
			{
				ex =ex.getCause();
				i++;
			}
			
			if (ex instanceof ConstraintViolationException)
			{
				ConstraintViolationException e2 = (ConstraintViolationException) ex;

				
					for (ConstraintViolation<?> violation : e2.getConstraintViolations())
					{
						StringBuilder sb = new StringBuilder();
						sb.append("Constraint Violation: \n");
						sb.append("Entity:" + violation.getRootBean());
						sb.append("Error: " + violation.getMessage() + "\n");
						sb.append(" on property: " + violation.getPropertyPath() + "\n");
						sb.append("Constraint:" + violation.getMessageTemplate());

						logger.error(sb.toString());
					}

				
			}
			throw e;
		}
		finally
		{
			
			t.close();
			// Reset the entity manager
			EntityManagerProvider.setCurrentEntityManager(null);
		}
	}

	@Override
	public void destroy()
	{
		// entityManagerFactory = null;
	}
}
