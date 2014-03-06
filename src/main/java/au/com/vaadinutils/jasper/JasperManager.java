package au.com.vaadinutils.jasper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.log4j.Logger;

import au.com.vaadinutils.dao.Transaction;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;

public class JasperManager
{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(JasperManager.class);

	public enum Disposition
	{
		inline, mixed

	}

	public enum Exporter
	{
		PDF, HTML;
	}

	final private JasperReport jasperReport;
	final Map<String, Object> boundParams = new HashMap<String, Object>();
	private JasperPrint jasper_print;
	private JasperSettings settings;
	private EntityManager em;

	/**
	 * 
	 * @param jasperReport
	 *            path to jasper report.
	 * @throws JRException
	 */
	public JasperManager(EntityManager em, File jasperReportFile, JasperSettings settings) throws JRException
	{
		Preconditions.checkArgument(jasperReportFile.exists(), "The passed Jasper Report File doesn't exist: "
				+ jasperReportFile.getAbsolutePath());

		this.em = em;
		this.settings = settings;
		this.jasperReport = (JasperReport) JRLoader.loadObject(jasperReportFile);

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

	public void fillReport() throws JRException
	{
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
	}

	boolean isEmpty()
	{
		Preconditions.checkArgument(jasper_print != null, "You must call fillReport first.");
		return (jasper_print.getPages().size() < 1);
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

	public RenderedReport export(Exporter exportMethod) throws JRException, IOException
	{

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final HashMap<String, byte[]> images = new HashMap<String, byte[]>();
		JRAbstractExporter exporter = null;

		switch (exportMethod)
		{
			case HTML:
			{
				exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, images);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "");
				break;
			}
			case PDF:
			{
				exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				break;
			}
			default:
			{
				throw new RuntimeException("Unsupported export option "+exportMethod);
				}
			
		}

		for (Entry<JRExporterParameter, Object> param : exporter.getParameters().entrySet())
		{
			logger.info(param.getKey() + ":" + param.getValue());

		}
		exporter.exportReport();

		for (Entry<JRExporterParameter, Object> param : exporter.getParameters().entrySet())
		{
			logger.info(param.getKey() + ":" + param.getValue());

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

		return new RenderedReport(out, imagesrcs, exportMethod);
	}

	public static class EmailBuilder
	{
		private JasperSettings settings;

		private ArrayList<DataSource> attachments = new ArrayList<DataSource>();
		private ArrayList<String> tos = new ArrayList<String>();
		private ArrayList<String> ccs = new ArrayList<String>();
		private ArrayList<String> bccs = new ArrayList<String>();
		private String subject;
		private String fromAddress;

		// Different body types
		private RenderedReport renderedReportBody;
		private String htmlBody;
		private String textBody = "Your email client does not support HTML messages";

		public EmailBuilder(JasperSettings settings)
		{
			this.settings = settings;
		}

		public EmailBuilder addTo(String toAddress)
		{
			this.tos.add(toAddress);
			return this;
		}

		public EmailBuilder addCC(String ccAddress)
		{
			this.ccs.add(ccAddress);
			return this;
		}

		public EmailBuilder addBCC(String bccAddress)
		{
			this.bccs.add(bccAddress);
			return this;
		}

		public EmailBuilder setSubject(String subject)
		{
			this.subject = subject;
			return this;
		}

		public EmailBuilder setFrom(String fromAddress)
		{
			this.fromAddress = fromAddress;
			return this;
		}

		public EmailBuilder setHtmlBody(RenderedReport renderedReport)
		{
			Preconditions.checkArgument(this.htmlBody == null, "You may only call one of the setXXXBody methods.");
			Preconditions.checkArgument(renderedReport.isHTML(), "The report has not been exported to HTML.");
			this.renderedReportBody = renderedReport;
			return this;
		}

		public EmailBuilder setHtmlBody(String htmlBody)
		{
			Preconditions.checkArgument(this.renderedReportBody == null,
					"You may only call one of the setXXXBody methods.");
			this.htmlBody = htmlBody;
			return this;
		}

		public EmailBuilder setTextBody(String body)
		{
			this.textBody = body;
			return this;
		}

		public EmailBuilder addAttachement(File attachement)
		{
			this.attachments.add(new FileDataSource(attachement));
			return this;
		}

		public EmailBuilder addAttachement(DataSource attachement)
		{
			this.attachments.add(attachement);
			return this;
		}

		public void send() throws EmailException
		{

			Preconditions.checkNotNull(fromAddress);
			Preconditions.checkNotNull(tos.size() > 0);
			Preconditions.checkNotNull(subject);
			Preconditions.checkNotNull(this.htmlBody != null || this.renderedReportBody != null,
					"You must specify a body.");

			ImageHtmlEmail email = new ImageHtmlEmail();
			if (this.renderedReportBody != null)
				email.setDataSourceResolver(new JasperDataSourceResovler(renderedReportBody));

			email.setDebug(true);
			email.setHostName(settings.getSmtpFQDN());
			email.setSmtpPort(settings.getSmtpPort());
			if (settings.isAuthRequired())
				email.setAuthentication(settings.getUsername(), settings.getPassword());
			if (settings.getUseSSL())
			{
				email.setSslSmtpPort(settings.getSmtpPort().toString());
				email.setSSLOnConnect(true);
				email.setSSLCheckServerIdentity(false);
			}
			email.setFrom(fromAddress);
			email.setBounceAddress(settings.getBounceEmailAddress());
			email.setSubject(subject);

			for (String to : this.tos)
				email.addTo(to);

			for (String cc : this.ccs)
				email.addCc(cc);

			for (String bcc : this.bccs)
				email.addBcc(bcc);

			if (this.htmlBody != null)
				email.setHtmlMsg(this.htmlBody);

			if (this.renderedReportBody != null)
				email.setHtmlMsg(this.renderedReportBody.getBodyAsHtml());

			email.setTextMsg(this.textBody);

			for (DataSource attachment : this.attachments)
				email.attach(attachment, attachment.getName(), attachment.getName());

			email.send();
		}
	}

	protected static String getJasperFile(final String dir, final String name) throws IOException
	{
		final File file = new File(dir, name);
		if (!file.exists())
			throw new FileNotFoundException(file.toString());
		return file.getCanonicalPath();
	}

}
