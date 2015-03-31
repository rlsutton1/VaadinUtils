package au.com.vaadinutils.util;

import com.vaadin.server.StreamResource.StreamSource;

public interface ContenctLengthProviderStreamSource extends StreamSource
{

	long getContentLength();

}
