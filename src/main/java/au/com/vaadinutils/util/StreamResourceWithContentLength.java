package au.com.vaadinutils.util;

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
		if (ss == null)
		{
			return null;
		}
		try
		{
			long contentLength = contentLengthProvider.getContentLength();
			final PartialDownloadStream ds = new PartialDownloadStream(ss.getStream(), getMIMEType(), getFilename());
			ds.setContentLength(contentLength);
			ds.setParameter("Content-Length", String.valueOf(contentLength));
			ds.setBufferSize(getBufferSize());
			ds.setCacheTime(getCacheTime());
			return ds;
		}
		catch (final Exception e)
		{
			if (ui != null)
			{
				ui.access(new Runnable()
				{

					@Override
					public void run()
					{
						Notification.show(e.getMessage(), Type.ERROR_MESSAGE);

					}
				});
			}
			else
			{
				ErrorWindow.showErrorWindow(e);
			}

			return null;
		}
	}

}
