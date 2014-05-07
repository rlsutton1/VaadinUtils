package au.com.vaadinutils.jasper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.activation.DataSource;

import org.apache.commons.mail.ByteArrayDataSource;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;

/*
 * Holds an in memory copy of a Rendered Jasper Report.
 */
public class RenderedReport
{
	private final InputStream reportBody;
	private final DataSource[] images;
	private OutputFormat exportMethod;

	RenderedReport(InputStream out, DataSource[] images, OutputFormat exportMethod)
	{
		this.reportBody = out;
		this.images = images;
		this.exportMethod = exportMethod;
	}

	public boolean isHTML()
	{
		return exportMethod == OutputFormat.HTML;
	}

	public boolean isPDF()
	{
		return exportMethod == OutputFormat.PDF;
	}

	public boolean isCsv()
	{
		return exportMethod == OutputFormat.CSV;
	}

	public String getBodyAsHtml() 
	{
		InputStreamReader isr = new InputStreamReader(reportBody);
		return isr.toString();

	}

	public DataSource getBodyAsDataSource(String fileName, AttachmentType type) throws IOException
	{

		final ByteArrayDataSource body = new ByteArrayDataSource(reportBody, type.toString());
		body.setName(fileName);

		return body;
	}

	public DataSource[] getImages()
	{
		return images;
	}

	public void close() throws IOException
	{
		reportBody.close();

	}

}