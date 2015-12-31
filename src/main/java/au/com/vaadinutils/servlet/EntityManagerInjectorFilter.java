package au.com.vaadinutils.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.LogManager;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.EntityWorker;

public class EntityManagerInjectorFilter implements Filter
{
	// private static transient Logger logger =
	org.apache.logging.log4j.Logger logger = LogManager.getLogger();

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
			logger.error(e1, e1);
		}
	}

	@Override
	public void destroy()
	{
		// entityManagerFactory = null;
	}
}
