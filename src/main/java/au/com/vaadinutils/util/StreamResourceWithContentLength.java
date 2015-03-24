package au.com.vaadinutils.util;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;

public class StreamResourceWithContentLength extends StreamResource
{

	private ContenctLengthProviderStreamSource contentLengthProvider;

	public StreamResourceWithContentLength(ContenctLengthProviderStreamSource streamSource, String filename)
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
		long contentLength = contentLengthProvider.getContentLength();
		final DownloadStream ds = new DownloadStream(ss.getStream(), getMIMEType(), getFilename());
		ds.setParameter("Content-Length", String.valueOf(contentLength));
		ds.setBufferSize(getBufferSize());
		ds.setCacheTime(getCacheTime());
		return ds;
	}

	

}
