package au.com.vaadinutils.jasper;

import net.sf.jasperreports.engine.export.JRHyperlinkProducer;
import net.sf.jasperreports.engine.export.JRHyperlinkProducerFactory;

public class CustomJRHyperlinkProducerFactory extends JRHyperlinkProducerFactory
{

	static final ThreadLocal<Boolean> useCustomHyperLinks = new ThreadLocal<Boolean>();

	public static void setUseCustomHyperLinks(boolean b)
	{
		useCustomHyperLinks.set(b);
	}

	@Override
	public JRHyperlinkProducer getHandler(String linkType)
	{
		if (useCustomHyperLinks.get() == null)
		{
			useCustomHyperLinks.set(false);
		}
		if (useCustomHyperLinks.get() == null)
		{
			useCustomHyperLinks.set(false);
		}
		if (useCustomHyperLinks.get())
		{
			return new CustomJRHyperlinkProducer();
		}

		return null;
	}

}
