package au.org.scoutmaster.jasper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.mail.ByteArrayDataSource;

import au.org.scoutmaster.jasper.JasperManager.Exporter;

public class RenderedReport
{
	private final ByteArrayOutputStream reportBody;
	private final DataSource[] images;
	private Exporter exportMethod;

	RenderedReport(ByteArrayOutputStream out, DataSource[] images, Exporter exportMethod)
	{
		this.reportBody = out;
		this.images = images;
		this.exportMethod = exportMethod;
	}

	public boolean isHTML()
	{
		return exportMethod == Exporter.HTML;
	}
	
	public boolean isPDF()
	{
		return exportMethod == Exporter.PDF;
	}

	public String getBodyAsHtml()
	{
		return reportBody.toString();
	}
	
	public DataSource getBodyAsDataSource() throws IOException
	{
		final ByteArrayDataSource body = new ByteArrayDataSource(reportBody.toString(), "text/html");
		body.setName("Body");
		return body;
	}
	
	OutputStream getBodyAsOutputStream()
	{
		return reportBody;
	}
	
	
	public DataSource[] getImages()
	{
		return images;
	}
	
}