package au.com.vaadinutils.jasper;

import java.util.LinkedList;
import java.util.List;

import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.extensions.ExtensionsRegistry;



public class ExtensionsRegistryFactory implements net.sf.jasperreports.extensions.ExtensionsRegistryFactory
{

	@Override
	public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties)
	{
		// TODO Auto-generated method stub
		return new ExtensionsRegistry()
		{
			
			@SuppressWarnings("unchecked")
			@Override
			public <T> List<T> getExtensions(Class<T> extensionType)
			{
				if (extensionType == net.sf.jasperreports.engine.export.JRHyperlinkProducerFactory.class)
				{
					List<T> list = new LinkedList<T>();
					list.add((T) new CustomJRHyperlinkProducerFactory());
					return list;
				}
				return new LinkedList<T>();
			}
		};
	}

}
