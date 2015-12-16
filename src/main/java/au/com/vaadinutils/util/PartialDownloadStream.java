package au.com.vaadinutils.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.server.Constants;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;

public class PartialDownloadStream extends DownloadStream
{

	private static final long serialVersionUID = 1L;

	private long contentLength;

	Logger logger = LogManager.getLogger();

	public PartialDownloadStream(InputStream stream, String contentType, String fileName)
	{
		super(stream, contentType, fileName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void writeResponse(VaadinRequest request, VaadinResponse response) throws IOException
	{
		if (getParameter("Location") != null)
		{
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", getParameter("Location"));
			return;
		}
		Integer start = null;
		Integer end = null;

		if (request.getHeader("Range") != null)
		{
			String rangeParam = request.getHeader("Range");
			String[] range = rangeParam.split("=")[1].split("-");
			start = Integer.parseInt(range[0]);
			if (range.length > 1)
			{
				end = Integer.parseInt(range[1]);
			}
			// Content-Range: bytes 42-1233/1234

			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}

		// Download from given stream
		final InputStream data = getStream();
		if (data == null)
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		OutputStream out = null;
		try
		{
			// Sets content type
			response.setContentType(getContentType());

			// Sets cache headers
			response.setCacheTime(getCacheTime());

			// suggest local filename from DownloadStream if
			// Content-Disposition
			// not explicitly set
			String contentDispositionValue = getParameter("Content-Disposition");
			if (contentDispositionValue == null)
			{
				contentDispositionValue = "filename=\"" + getFileName() + "\"";
				response.setHeader("Content-Disposition", contentDispositionValue);
			}

			// Copy download stream parameters directly
			// to HTTP headers.
			final Iterator<String> i = getParameterNames();
			if (i != null)
			{
				while (i.hasNext())
				{
					final String param = i.next();
					response.setHeader(param, getParameter(param));
				}
			}

			// Content-Range: bytes 42-1233/1234
			if (start != null && end != null)
			{
				logger.warn("untested, may be broken");
				response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + (contentLength));
				response.setHeader("Content-Length", String.valueOf(end - start));

			}
			if (start != null && end == null)
			{
				response.setHeader("Content-Range", "bytes " + start + "-" + (contentLength - 1) + "/"
						+ (contentLength));
				response.setHeader("Content-Length", String.valueOf(contentLength - start));

			}

			int bufferSize = getBufferSize();
			if (bufferSize <= 0 || bufferSize > Constants.MAX_BUFFER_SIZE)
			{
				bufferSize = Constants.DEFAULT_BUFFER_SIZE;
			}

			final byte[] buffer = new byte[bufferSize];
			int bytesRead = 0;

			out = response.getOutputStream();

			if (start == null)
			{
				start = 0;
			}

			long totalWritten = 0;
			long counter = 0;
			while ((bytesRead = data.read(buffer)) > 0)
			{

				int bufferStart = 0;
				int bufferEnd = bytesRead;
				if (counter + bytesRead >= start)
				{
					if (counter < start && counter + bytesRead >= start)
					{
						bufferStart = (int) (start - counter);
					}

					out.write(buffer, bufferStart, bufferEnd);

					totalWritten += bufferEnd - bufferStart;
					if (totalWritten >= buffer.length)
					{
						// Avoid chunked encoding for small resources
						out.flush();
					}
				}
				counter += bytesRead;
			}
			if (totalWritten != contentLength - (start))
			{
				logger.error("Error {}", (contentLength - (start)) - totalWritten);
			}
		}
		catch (Exception e)
		{
			logger.error(e);
		}
		finally
		{
			tryToCloseStream(out);
			tryToCloseStream(data);
		}

	}

	/**
	 * Helper method that tries to close an output stream and ignores any
	 * exceptions.
	 *
	 * @param out
	 *            the output stream to close, <code>null</code> is also
	 *            supported
	 */
	static void tryToCloseStream(OutputStream out)
	{
		try
		{
			// try to close output stream (e.g. file handle)
			if (out != null)
			{
				out.close();
			}
		}
		catch (IOException e1)
		{
			// NOP
		}
	}

	/**
	 * Helper method that tries to close an input stream and ignores any
	 * exceptions.
	 *
	 * @param in
	 *            the input stream to close, <code>null</code> is also supported
	 */
	static void tryToCloseStream(InputStream in)
	{
		try
		{
			// try to close output stream (e.g. file handle)
			if (in != null)
			{
				in.close();
			}
		}
		catch (IOException e1)
		{
			// NOP
		}
	}

	public void setContentLength(long contentLength)
	{
		this.contentLength = contentLength;

	}
}
