package au.com.vaadinutils.jasper;

import java.io.IOException;

import javax.activation.DataSource;

import org.apache.commons.mail.DataSourceResolver;

public class JasperDataSourceResolver implements DataSourceResolver
{
	
	private RenderedReport renderedReport;

	public JasperDataSourceResolver(RenderedReport renderedReport)
	{
		this.renderedReport = renderedReport;
	}

	@Override
	public DataSource resolve(String resourceLocation) throws IOException
	{
		return resolve(resourceLocation, false);
	}

	@Override
	public DataSource resolve(String resourceLocation, boolean isLenient) throws IOException
	{
		DataSource found = null;
		
		for (DataSource image: renderedReport.getImages())
		{
			if (image.getName().equals(resourceLocation))
			{
				found = image;
				break;
			}
		}
		if (found == null && !isLenient)
			throw new IOException("Image '" + resourceLocation + " image not found.");
		
		return found;
	}

}
