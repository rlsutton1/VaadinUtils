/*
 * VaadinJasperPrintServlet.java
 * Designed to print a JasperReport report by name to the requested
 * output sink.
 * The default output sink is HTML.
 *
 *
 *
 * Created on 12 July 2002, 13:54
 */

package au.com.vaadinutils.jasper.servlet;

import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author bsutton
 * @version
 */
public class VaadinJasperPrintServlet extends HttpServlet
{

	public static final String IMAGES_MAP = "IMAGES_MAP";
	private static final long serialVersionUID = -4735461255420730963L;
	static Logger logger = LogManager.getLogger();

	/**
	 * Called to show an image that has been loaded into the session be a prior
	 * call to doPrint.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		@SuppressWarnings("unchecked")
		Map<String, byte[]> imagesMap = (Map<String, byte[]>) request.getSession().getAttribute(IMAGES_MAP);

		try
		{
			if (imagesMap != null)
			{
				String imageName = request.getParameter("image");
				if (imageName != null)
				{
					byte[] imageData = imagesMap.get(imageName);

					if (imageData == null)
						throw new IllegalStateException(
								"The passed image (" + imageName + " has not been loaded into the session.");

					response.setContentLength(imageData.length);
					ServletOutputStream out = response.getOutputStream();
					out.write(imageData, 0, imageData.length);
					out.flush();
					out.close();
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

}
