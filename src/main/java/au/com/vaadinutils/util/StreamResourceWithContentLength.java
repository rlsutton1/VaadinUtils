package au.com.vaadinutils.util;

import org.apache.logging.log4j.Logger;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import au.com.vaadinutils.errorHandling.ErrorWindow;

public class StreamResourceWithContentLength extends StreamResource
{

	private ContentLengthProviderStreamSource contentLengthProvider;
	UI ui = UI.getCurrent();

	Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	public StreamResourceWithContentLength(ContentLengthProviderStreamSource streamSource, String filename)
	{
		super(streamSource, filename);
		this.contentLengthProvider = streamSource;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public DownloadStream getStream()
	{
		final StreamSource ss = getStreamSource();

		PartialDownloadStream ds = new PartialDownloadStream(null, getMIMEType(), getFilename());

		if (ss != null)
		{
			try
			{
				ds.setStream(ss.getStream());
				long contentLength = contentLengthProvider.getContentLength();
				ds.setContentLength(contentLength);
				ds.setParameter("Content-Length", String.valueOf(contentLength));
				ds.setBufferSize(getBufferSize());
				ds.setCacheTime(getCacheTime());

			}
			catch (final Exception e)
			{
				logger.error(e, e);
				if (ui != null)
				{
					ui.access(new Runnable()
					{

						@Override
						public void run()
						{
							Notification.show(e.getClass().getSimpleName(), Type.ERROR_MESSAGE);

						}
					});
				}
				else
				{
					ErrorWindow.showErrorWindow(e);
				}

			}
		}
		return ds;
	}

}
