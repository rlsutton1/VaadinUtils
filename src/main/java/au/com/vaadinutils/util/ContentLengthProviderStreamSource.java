package au.com.vaadinutils.util;

import com.vaadin.server.StreamResource.StreamSource;

public interface ContentLengthProviderStreamSource extends StreamSource
{

	long getContentLength();

}
