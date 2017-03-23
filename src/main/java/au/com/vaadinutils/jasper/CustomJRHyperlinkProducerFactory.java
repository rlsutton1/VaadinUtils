package au.com.vaadinutils.jasper;

import net.sf.jasperreports.engine.export.JRHyperlinkProducer;
import net.sf.jasperreports.engine.export.JRHyperlinkProducerFactory;

public class CustomJRHyperlinkProducerFactory extends JRHyperlinkProducerFactory
{

	@Override
	public JRHyperlinkProducer getHandler(String linkType)
	{

		return new CustomJRHyperlinkProducer();
	}

}
