package au.com.vaadinutils.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticContentServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	Logger logger = LogManager.getLogger();
	private ServletContext sc;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		sc = config.getServletContext();

	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ServletContext servletContext = request.getServletContext();
		String requestURI = java.net.URLDecoder.decode(request.getRequestURI(), "UTF-8");
		// strip the context as otherwise it gets duplicated in the next step
		String relativePath = requestURI.replace(servletContext.getContextPath(), "");
		relativePath = requestURI.replace(servletContext.getContextPath(), "");
		String file = servletContext.getRealPath(relativePath);
		File resource = new File(file);

		if (!resource.exists())
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;

		}

		send(request, response, resource);

	}

	public void send(HttpServletRequest request, HttpServletResponse response, File resource) throws IOException
	{
		// Find the modification timestamp
		long lastModifiedTime = resource.lastModified();

		// Remove milliseconds to avoid comparison problems (milliseconds
		// are not returned by the browser in the "If-Modified-Since"
		// header).
		lastModifiedTime = lastModifiedTime - lastModifiedTime % 1000;

		if (browserHasNewestVersion(request, lastModifiedTime))
		{
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		// Set type mime type if we can determine it based on the filename

		final String mimetype = sc.getMimeType(resource.getName());
		if (mimetype != null)
		{
			response.setContentType(mimetype);
		}

		// Provide modification timestamp to the browser if it is known.
		if (lastModifiedTime > 0)
		{
			response.setDateHeader("Last-Modified", lastModifiedTime);

			String cacheControl = "public, max-age=0, must-revalidate";
			int resourceCacheTime = getCacheTime(resource.getName());
			if (resourceCacheTime > 0)
			{
				cacheControl = "max-age=" + String.valueOf(resourceCacheTime);
			}
			response.setHeader("Cache-Control", cacheControl);
		}

		writeStaticResourceResponse(request, response, resource);
	}

	/**
	 * Calculates the cache lifetime for the given filename in seconds. By
	 * default filenames containing ".nocache." return 0, filenames containing
	 * ".cache." return one year, all other return the value defined in the
	 * web.xml using resourceCacheTime (defaults to 1 hour).
	 *
	 * @param filename
	 * @return cache lifetime for the given filename in seconds
	 */
	protected int getCacheTime(String filename)
	{
		/*
		 * GWT conventions:
		 *
		 * - files containing .nocache. will not be cached.
		 *
		 * - files containing .cache. will be cached for one year.
		 *
		 * https://developers.google.com/web-toolkit/doc/latest/
		 * DevGuideCompilingAndDebugging#perfect_caching
		 */
		if (filename.contains(".nocache."))
		{
			return 0;
		}
		if (filename.contains(".cache."))
		{
			return 60 * 60 * 24 * 365;
		}
		/*
		 * For all other files, the browser is allowed to cache for 1 hour
		 * without checking if the file has changed. This forces browsers to
		 * fetch a new version when the Vaadin version is updated. This will
		 * cause more requests to the servlet than without this but for high
		 * volume sites the static files should never be served through the
		 * servlet.
		 */
		return 60 * 60;
	}

	private void writeStaticResourceResponse(HttpServletRequest request, HttpServletResponse response, File file)
			throws IOException
	{
		// this always will point to the root of the web site
		ServletOutputStream outStream = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;

		try
		{
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			byte[] buf = new byte[4 * 1024]; // 4K buffer
			int bytesRead;
			outStream = response.getOutputStream();
			while ((bytesRead = bis.read(buf)) != -1)
			{
				outStream.write(buf, 0, bytesRead);
			}
		}
		catch (Exception e)
		{
			logger.warn("EWengine: file not found or unable to read: '" + file + "'");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		finally
		{
			if (bis != null)
				bis.close();
			if (fis != null)
				fis.close();
			if (outStream != null)
				outStream.close();
		}

	}

	/**
	 * Checks if the browser has an up to date cached version of requested
	 * resource. Currently the check is performed using the "If-Modified-Since"
	 * header. Could be expanded if needed.
	 *
	 * @param request
	 *            The HttpServletRequest from the browser.
	 * @param resourceLastModifiedTimestamp
	 *            The timestamp when the resource was last modified. 0 if the
	 *            last modification time is unknown.
	 * @return true if the If-Modified-Since header tells the cached version in
	 *         the browser is up to date, false otherwise
	 */
	private boolean browserHasNewestVersion(HttpServletRequest request, long resourceLastModifiedTimestamp)
	{
		if (resourceLastModifiedTimestamp < 1)
		{
			// We do not know when it was modified so the browser cannot have an
			// up-to-date version
			return false;
		}
		/*
		 * The browser can request the resource conditionally using an
		 * If-Modified-Since header. Check this against the last modification
		 * time.
		 */
		try
		{
			// If-Modified-Since represents the timestamp of the version cached
			// in the browser
			long headerIfModifiedSince = request.getDateHeader("If-Modified-Since");

			if (headerIfModifiedSince >= resourceLastModifiedTimestamp)
			{
				// Browser has this an up-to-date version of the resource
				return true;
			}
		}
		catch (Exception e)
		{
			// Failed to parse header. Fail silently - the browser does not have
			// an up-to-date version in its cache.
		}
		return false;
	}

	/**
	 * used by StaticFilter to initialize the servletContext
	 *
	 * @param servletContext
	 */
	public void setContext(ServletContext servletContext)
	{
		sc = servletContext;

	}

}