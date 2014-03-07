package au.com.vaadinutils.jasper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataSource;
import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.Transaction;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;

public class JasperManager
{
	private static Logger logger = LogManager.getLogger(JasperManager.class);

	public enum Disposition
	{
		inline, mixed

	}

	public enum OutputFormat
	{
		PDF
		{
			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}
		},
		HTML
		{
			@Override
			public String getMimeType()
			{
				return "text/html";
			}
		},
		CSV
		{
			@Override
			public String getMimeType()
			{
				return "text/csv";
			}
		};

		abstract public String getMimeType();
	}

	final private JasperReport jasperReport;
	final Map<String, Object> boundParams = new HashMap<String, Object>();
	private JasperSettings settings;
	private EntityManager em;
	private String reportName;

	/**
	 * 
	 * @param jasperReport
	 *            path to jasper report.
	 * @throws JRException
	 */
	public JasperManager(EntityManager em, String reportName, JasperSettings settings) throws JRException
	{
		Preconditions.checkArgument(settings.getReportFile(reportName).exists(),
				"The passed Jasper Report File doesn't exist: " + settings.getReportFile(reportName).getAbsolutePath());

		this.em = em;
		this.settings = settings;
		this.reportName = reportName;
		this.jasperReport = (JasperReport) JRLoader.loadObject(settings.getReportFile(reportName));

	}

	public JRParameter[] getParameters()
	{
		return jasperReport.getParameters();
	}

	public void bindParameter(String parameterName, String parameterValue)
	{
		Preconditions.checkArgument(paramExists(parameterName), "The passed Jasper Report parameter: " + parameterName
				+ " does not existing on the Report");

		boundParams.put(parameterName, parameterValue);
	}

	public void bindParameters(Map<String, Object> parameters)
	{
		for (String parameterName : parameters.keySet())
		{
			Preconditions.checkArgument(paramExists(parameterName), "The passed Jasper Report parameter: "
					+ parameterName + " does not existing on the Report");

			boundParams.put(parameterName, parameters.get(parameterName));
		}
	}

	private JasperPrint fillReport() throws JRException
	{
		JasperPrint jasper_print;

		Transaction t = new Transaction(em);
		try
		{

			java.sql.Connection connection = em.unwrap(java.sql.Connection.class);

			jasper_print = JasperFillManager.fillReport(jasperReport, boundParams, connection);

			t.commit();
		}
		finally
		{

			t.close();
		}
		
		return jasper_print;
	}


	public JasperSettings getSettings()
	{
		return this.settings;
	}

	private boolean paramExists(String parameterName)
	{
		boolean exists = false;

		for (JRParameter param : getParameters())
		{
			if (param.getName().equals(parameterName))
			{
				exists = true;
				break;
			}
		}
		return exists;
	}

	public RenderedReport export(OutputFormat exportMethod) throws JRException, IOException
	{

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final HashMap<String, byte[]> images = new HashMap<String, byte[]>();
		JRAbstractExporter exporter = null;
		
		JasperPrint jasper_print = fillReport();
		
		String renderedName = this.jasperReport.getName();


		switch (exportMethod)
		{
			case HTML:
			{
				exporter = new JRHtmlExporter();

				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, images);

				String context = VaadinServlet.getCurrent().getServletContext().getContextPath();
				int contextIndex = Page.getCurrent().getLocation().toString().indexOf(context);
				String baseurl = Page.getCurrent().getLocation().toString().substring(0, contextIndex + context.length() + 1);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, baseurl + "images?image=");
				
				renderedName += ".htm";
				break;
			}
			case PDF:
			{
				exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				renderedName += ".pdf";
				break;
			}
			case CSV:
			{
				exporter = new JRCsvExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				renderedName += ".csv";
				break;
			}
			default:
			{
				throw new RuntimeException("Unsupported export option " + exportMethod);
			}

		}

		if (logger.isDebugEnabled())
			for (Entry<JRExporterParameter, Object> param : exporter.getParameters().entrySet())
			{
				logger.debug("{} : {}", param.getKey(), param.getValue());

			}

		// InputStream stream =
		// JasperRunManager.runReportToPdf(getClass().getClassLoader().getResourceAsStream(settings.getReportFile(this.reportName)),
		// map, con);

		if (logger.isDebugEnabled())
			for (Entry<JRExporterParameter, Object> param : exporter.getParameters().entrySet())
			{
				logger.debug("{} : {}", param.getKey(), param.getValue());

			}

		final DataSource[] imagesrcs = (images.size() <= 0) ? null : new DataSource[images.size()];
		if (imagesrcs != null)
		{
			int xi = 0;
			for (Map.Entry<String, byte[]> entry : images.entrySet())
			{
				ByteArrayDataSource image = new ByteArrayDataSource(entry.getValue(), "image/gif");
				image.setName(entry.getKey());
				imagesrcs[xi++] = image;
			}
		}

		exporter.exportReport();

		return new RenderedReport(this, renderedName, out, imagesrcs, exportMethod);
	}

	protected static String getJasperFile(final String dir, final String name) throws IOException
	{
		final File file = new File(dir, name);
		if (!file.exists())
			throw new FileNotFoundException(file.toString());
		return file.getCanonicalPath();
	}

	public String getReportName()
	{
		return reportName;
	}

}
