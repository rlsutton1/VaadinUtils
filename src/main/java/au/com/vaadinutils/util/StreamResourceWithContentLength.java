package au.com.vaadinutils.util;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;

public class StreamResourceWithContentLength extends StreamResource
{

	private int contentLength;

	public StreamResourceWithContentLength(StreamSource streamSource, String filename, int contentLength)
	{
		super(streamSource, filename);
		this.contentLength = contentLength;
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
		final DownloadStream ds = new DownloadStream(ss.getStream(), getMIMEType(), getFilename());
		ds.setParameter("Content-Length", String.valueOf(contentLength));
		ds.setBufferSize(getBufferSize());
		ds.setCacheTime(getCacheTime());
		return ds;
	}

}
