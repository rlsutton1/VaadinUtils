package au.com.vaadinutils.jasper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.mail.ByteArrayDataSource;

import au.com.vaadinutils.jasper.JasperManager.OutputFormat;

import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

/*
 * Holds an in memory copy of a Rendered Jasper Report.
 */
public class RenderedReport
{
	private final ByteArrayOutputStream reportBody;
	private final DataSource[] images;
	private OutputFormat exportMethod;
	private String name;
	@SuppressWarnings("unused")
	private JasperManager manager;

	RenderedReport(JasperManager manager, String reportName, ByteArrayOutputStream out, DataSource[] images, OutputFormat exportMethod)
	{
		this.manager = manager;
		this.name = reportName;
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

	public Resource getBodyAsResource()
	{
		//new ByteArrayDataSource(entry.getValue(), "image/gif");
		
		// TODO: can we stream this.
		JasperStreamSource jss = new JasperStreamSource(new ByteArrayInputStream(reportBody.toByteArray()));
		

//        StreamResource resource = new JasperStreamSource(source, "TokenReport.pdf", getApplication());
//        resource.setMIMEType("application/pdf");
//
		
	
//
		StreamResource streamResource = new StreamResource(jss, this.name);
		streamResource.setMIMEType(exportMethod.getMimeType());
		
		return streamResource;
	}
	
	static class JasperStreamSource implements StreamSource
	{
		private static final long serialVersionUID = 1L;
		private InputStream stream;

		JasperStreamSource(InputStream stream)
		{
			this.stream = stream;
//	                byte[] b = null;
//	                try {
//	                	RenderReported.this.manager.
//	                } catch (JRException ex) {
//	                    Logger.getLogger(TokenForm.class.getName()).log(Level.SEVERE, null, ex);
//	                }
//	                stream = new ByteArrayInputStream(b);
//	        };
			
			
		}

		@Override
		public InputStream getStream()
		{
			
			return this.stream;
		}
		
		
	}
	
//	 try {
//         //FileOutputStream of = new FileOutputStream("TokenReport.pdf");
//         //JasperRunManager.runReportToPdfStream(getClass().getClassLoader().getResourceAsStream("reports/TokenReport.jasper"), of, map, con);
//
//         StreamResource.StreamSource source = new StreamResource.StreamSource() {
//
//             public InputStream getStream() {
//                 byte[] b = null;
//                 try {
//                     b = JasperRunManager.runReportToPdf(getClass().getClassLoader().getResourceAsStream("reports/TokenReport.jasper"), map, con);
//                 } catch (JRException ex) {
//                     Logger.getLogger(TokenForm.class.getName()).log(Level.SEVERE, null, ex);
//                 }
//                 //throw new UnsupportedOperationException("Not supported yet.");
//                 return new ByteArrayInputStream(b);
//             }
//         };
//
//         StreamResource resource = new StreamResource(source, "TokenReport.pdf", getApplication());
//         resource.setMIMEType("application/pdf");

//         Window w = new Window("Token Form");
//         w.setSizeFull();
//         //getWindow().addWindow(w);
//
//         Embedded e = new Embedded();
//         e.setMimeType("application/pdf");
//         e.setType(Embedded.TYPE_OBJECT);
//         e.setSizeFull();
//         e.setSource(resource);
//         e.setParameter("Content-Disposition", "attachment; filename=" + resource.getFilename());
//
//         w.addComponent(e);

//         getApplication().getMainWindow().open(resource, "_new");
//         
//
//     } catch (Exception ex) {
//         Logger.getLogger(TokenForm.class.getName()).log(Level.SEVERE, null, ex);
//     }
//	
}