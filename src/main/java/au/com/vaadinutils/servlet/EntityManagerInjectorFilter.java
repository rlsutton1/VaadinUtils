package au.com.vaadinutils.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.EntityWorker;
import au.com.vaadinutils.errorHandling.ErrorWindow;

public class EntityManagerInjectorFilter implements Filter
{
	// private static transient Logger logger =
	org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
			final FilterChain filterChain) throws IOException, ServletException
	{
		try
		{
			EntityManagerProvider.setThreadLocalEntityManager(new EntityWorker<Void>()
			{

				@Override
				public Void exec() throws Exception
				{
					filterChain.doFilter(servletRequest, servletResponse);
					return null;
				}
			});
		}
		catch (Exception e1)
		{
			ErrorWindow.showErrorWindow(e1);
		}
	}

	@Override
	public void destroy()
	{
		// entityManagerFactory = null;
	}
}
