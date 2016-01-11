package au.com.vaadinutils.jasper;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.activation.DataSource;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.UI;

import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.servlet.VaadinJasperPrintServlet;
import au.com.vaadinutils.jasper.ui.CleanupCallback;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.fill.FillListener;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.web.servlets.AsyncJasperPrintAccessor;
import net.sf.jasperreports.web.servlets.ReportExecutionStatus;

public class JasperManager implements Runnable
{
	private JasperReport jasperReport;

	private static transient Logger logger = LogManager.getLogger(JasperManager.class);

	private final Map<String, Object> boundParams = new HashMap<String, Object>();

	private CustomAsynchronousFillHandle fillHandle;

	volatile private boolean stop;

	private JasperReportProperties reportProperties;

	private final static int reportLimit = Math.max(Runtime.getRuntime().availableProcessors() / 2, 1);

	private final static Semaphore concurrentLimit = new Semaphore(reportLimit, true);

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

			@Override
			public String getFileExtension()
			{
				return ".pdf";
			}

			@Override
			public AttachmentType getAttachementType()
			{
				return AttachmentType.PDF;
			}
		},
		HTML
		{
			@Override
			public String getMimeType()
			{
				return "text/html";
			}

			@Override
			public String getFileExtension()
			{
				return ".html";
			}

			@Override
			public AttachmentType getAttachementType()
			{
				return AttachmentType.HTML;
			}
		},
		CSV
		{
			@Override
			public String getMimeType()
			{
				return "text/csv";
			}

			@Override
			public String getFileExtension()
			{
				return ".csv";
			}

			@Override
			public AttachmentType getAttachementType()
			{
				return AttachmentType.CSV;
			}
		};

		abstract public String getMimeType();

		abstract public String getFileExtension();

		abstract public AttachmentType getAttachementType();

	}

	/**
	 * key: report filename <br>
	 * value: last time it was compiled
	 */
	final static Map<String, Long> compiledReports = new ConcurrentHashMap<String, Long>();

	/**
	 *
	 * @param jasperReport
	 *            path to jasper report.
	 * @throws JRException
	 */
	public JasperManager(JasperReportProperties reportProperties)
	{

		this.reportProperties = reportProperties;

	}

	private void compileReport()
	{
		try
		{
			String suppliedFileName = reportProperties.getReportFileName();
			if (suppliedFileName.contains("."))
			{
				suppliedFileName = suppliedFileName.substring(0, suppliedFileName.indexOf("."));
			}
			String reportFileName = suppliedFileName + ".jasper";
			String reportDesignName = suppliedFileName + ".jrxml";

			File designFile = new File(reportProperties.getReportFolder(), reportDesignName);
			Preconditions.checkArgument(designFile.exists(),
					"The passed Jasper Report File doesn't exist: " + designFile.getAbsolutePath());

			File sourcePath = designFile.getParentFile();

			if (!compiledReports.containsKey(reportFileName)
					|| compiledReports.get(reportFileName) < designFile.lastModified() || reportProperties.isDevMode())
			{
				// compileReport(getDesignFile(sourcePath, reportDesignName),
				// sourcePath, sourcePath, reportDesignName);

				JasperReportCompiler jasperReportCompiler = new JasperReportCompiler();
				JasperDesign design = jasperReportCompiler.getDesignFile(sourcePath, reportDesignName);

				String templateName = reportProperties.getHeaderFooterTemplateName();
				if (templateName != null)
				{
					JasperDesign headerTemplate = jasperReportCompiler.getDesignFile(sourcePath, templateName);

					replaceHeader(design, headerTemplate);
				}
				setCSVOptions(design);

				jasperReportCompiler.compileReport(design, sourcePath, sourcePath, suppliedFileName);
			}
			this.jasperReport = (JasperReport) JRLoader
					.loadObject(new File(reportProperties.getReportFolder(), reportFileName));
			compiledReports.put(reportFileName, designFile.lastModified());

		}
		catch (Throwable e)
		{
			logger.error(e, e);
			throw new RuntimeException("Bad report compilation");
		}
	}

	/**
	 * replaces the header, footer, nodata band and dynamically adds user
	 * friendly report parameter display to the report.
	 *
	 * @param designFile
	 * @param template
	 * @throws JRException
	 */
	private void replaceHeader(JasperDesign designFile, JasperDesign template) throws JRException
	{
		JRBand title = template.getTitle();
		JRDesignBand newTitle = new JRDesignBand();

		int margin = 50;
		designFile.setLeftMargin(margin);
		designFile.setRightMargin(margin);
		designFile.setTopMargin(margin);
		designFile.setBottomMargin(margin);

		double pageWidth = designFile.getPageWidth() + (margin * 2);

		double ratio = 0.75; // landscape;
		if (designFile.getPageHeight() / pageWidth >= 1)
		{
			ratio = 1.5; // portrait
		}
		int pageHeight = (int) (pageWidth * ratio);
		designFile.setPageWidth((int) pageWidth);
		designFile.setPageHeight(pageHeight);

		int maxY = determineSizeOfTemplateBandAndReplaceTitlePlaceHolder(designFile, title, newTitle, margin);

		maxY += 2;

		mergeExistingTitleWithTemplateTitle(designFile, newTitle, maxY);

		JRBand footer = replaceFooterWithTemplateFooter(designFile, template, margin);

		designFile.setPageFooter(footer);

		JRDesignBand noDataBand = new JRDesignBand();
		int noDataHeight = determineSizeOfTemplateBandAndReplaceTitlePlaceHolder(designFile, template.getNoData(),
				noDataBand, margin);
		designFile.setNoData(noDataBand);
		noDataBand.setHeight(noDataHeight);
		designFile.setWhenNoDataType(WhenNoDataTypeEnum.NO_DATA_SECTION);

	}

	/**
	 * replace the footer with the template footer, including update the place
	 * holder for the report title in the footer
	 *
	 * @param designFile
	 * @param template
	 * @param margin
	 * @return
	 */
	private JRBand replaceFooterWithTemplateFooter(JasperDesign designFile, JasperDesign template, int margin)
	{
		JRBand footer = template.getPageFooter();
		for (JRElement element : footer.getElements())
		{
			if (element instanceof JRDesignElement)
			{
				if (element instanceof JRTextField)
				{
					JRTextField st = (JRTextField) element;

					JRDesignExpression expr = (JRDesignExpression) st.getExpression();
					expr.setText("\"" + reportProperties.getReportTitle() + "\"+" + expr.getText());
					st.setWidth((designFile.getPageWidth() - st.getX()) - (margin * 2));

				}

			}

		}
		return footer;
	}

	/**
	 * we are now dropping the existing title on the ground in favour of the
	 * dynamically generated one based on the template
	 *
	 * @param designFile
	 * @param newTitle
	 * @param yoffset
	 */
	private void mergeExistingTitleWithTemplateTitle(JasperDesign designFile, JRDesignBand newTitle, int yoffset)
	{
		int maxY = 0;
		// for (JRElement element : designFile.getTitle().getElements())
		// {
		//
		// JRDesignElement de = (JRDesignElement) element;
		// de.setY(de.getY() + yoffset);
		// maxY = Math.max(maxY, de.getY() + element.getHeight());
		// newTitle.addElement(element);
		// }

		newTitle.setHeight(Math.max(maxY + 2, yoffset + 2));
		designFile.setTitle(newTitle);

	}

	/**
	 * replace the place holder in the targetBand with the report title, also
	 * add a set of dynamically generated user friendly report parameter fields
	 * to this band and add parameters to the report for them if not already
	 * present.
	 *
	 * @param designFile
	 * @param templateBand
	 * @param targetBand
	 * @param margin
	 * @return
	 * @throws JRException
	 */
	private int determineSizeOfTemplateBandAndReplaceTitlePlaceHolder(JasperDesign designFile, JRBand templateBand,
			JRDesignBand targetBand, int margin) throws JRException
	{
		int maxY = 0;
		for (JRElement element : templateBand.getElements())
		{

			JRDesignElement de = (JRDesignElement) element;
			if (element instanceof JRStaticText)
			{
				JRStaticText st = (JRStaticText) element;
				if (st.getText().equalsIgnoreCase("report name place holder"))
				{
					st.setText(reportProperties.getReportTitle());
					st.setWidth((designFile.getPageWidth() - st.getX()) - (margin * 2));
				}
			}

			if (element instanceof JRDesignImage)
			{
				JRDesignImage im = (JRDesignImage) element;
				String expr = im.getExpression().getText();
				if (expr.contains("logo.png"))
				{
					int height = 140;
					final int imageWidth = designFile.getPageWidth() - (margin * 2);
					String fileName = reportProperties.generateDynamicHeaderImage(imageWidth, height,
							reportProperties.getReportTitle());
					im.setWidth(imageWidth);
					// im.setHeight(height);
					im.setX(-8);

					expr = expr.replace("logo.png", fileName);
					im.setExpression(new JRDesignExpression(expr));
				}
			}

			maxY = Math.max(maxY, de.getY() + de.getHeight());

			targetBand.addElement(de);

		}

		JRDesignStaticText paramElement = new JRDesignStaticText();
		paramElement.setText("Parameters");
		paramElement.setWidth(525);
		paramElement.setHeight(15);
		paramElement.setBackcolor(new Color(95, 96, 98));
		paramElement.setForecolor(new Color(255, 255, 255));
		paramElement.setMode(ModeEnum.OPAQUE);

		paramElement.setX(0);
		paramElement.setY(maxY + 2);
		paramElement.setFontName("Arial");
		paramElement.setBold(true);
		paramElement.setFontSize(12);
		paramElement.setHorizontalAlignment(HorizontalAlignEnum.CENTER);
		paramElement.setVerticalAlignment(VerticalAlignEnum.MIDDLE);

		targetBand.addElement(paramElement);
		maxY = paramElement.getY() + paramElement.getHeight();

		maxY = addParametersToDisplayReportParameters(designFile, targetBand, maxY);
		return maxY;
	}

	/**
	 * add user friendly paramters to the band and report port parameters if not
	 * already present
	 *
	 * @param designFile
	 * @param targetBand
	 * @param maxY
	 * @return
	 * @throws JRException
	 */
	private int addParametersToDisplayReportParameters(JasperDesign designFile, JRDesignBand targetBand, int maxY)
			throws JRException
	{
		for (ReportParameter<?> param : reportProperties.getFilterBuilder().getReportParameters())
		{
			if (param.displayInreport())
			{
				for (String parameterName : param.getParameterNames())
				{
					JRDesignStaticText labelElement = new JRDesignStaticText();

					String strippedLabel = param.getLabel().replaceAll("ReportParameter", "");

					labelElement.setText(strippedLabel);
					labelElement.setWidth(125);
					labelElement.setHeight(20);
					labelElement.setBackcolor(new Color(208, 208, 208));
					labelElement.setMode(ModeEnum.OPAQUE);
					labelElement.setVerticalAlignment(VerticalAlignEnum.MIDDLE);

					labelElement.setX(0);
					labelElement.setY(maxY);
					labelElement.setFontName("SansSerif");
					labelElement.setFontSize(12);
					targetBand.addElement(labelElement);

					JRDesignTextField valueElement = new JRDesignTextField();
					valueElement.setExpression(new JRDesignExpression("$P{ParamDisplay-" + parameterName + "}"));
					valueElement.setWidth(400);
					valueElement.setHeight(20);
					valueElement.setBackcolor(new Color(208, 208, 208));
					valueElement.setMode(ModeEnum.OPAQUE);

					valueElement.setX(125);
					valueElement.setY(maxY);
					valueElement.setFontName("SansSerif");
					valueElement.setFontSize(12);
					valueElement.setVerticalAlignment(VerticalAlignEnum.MIDDLE);

					targetBand.addElement(valueElement);
					maxY = valueElement.getY() + valueElement.getHeight();

					if (!designFile.getParametersMap().containsKey("ParamDisplay-" + parameterName))
					{
						JRDesignParameter parameter = new JRDesignParameter();
						parameter.setName("ParamDisplay-" + parameterName);
						parameter.setValueClass(String.class);

						parameter.setForPrompting(false);

						designFile.addParameter(parameter);
					}
				}
			}
		}
		return maxY;
	}

	private void setCSVOptions(JasperDesign designFile)
	{
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.columnHeader",
				"columnHeader");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.title", "title");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.pageHeader", "pageHeader");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.pageFooter", "pageFooter");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.footer", "footer");
	}

	public JRParameter[] getParameters()
	{
		return jasperReport.getParameters();
	}

	public boolean paramExists(String parameterName)
	{
		return getParameter(parameterName) != null;
	}

	public JRParameter getParameter(String parameterName)
	{
		JRParameter result = null;

		for (JRParameter param : jasperReport.getParameters())
		{
			if (param.getName().equals(parameterName))
			{
				result = param;
				break;
			}
		}
		return result;

	}

	/**
	 * Binds a value to a report parameter.
	 *
	 * Essentially a report can have a no. of named parameters which are used to
	 * filter the report or display on the report. This method allows you to set
	 * the parameters value at runtime.
	 *
	 * @param parameterName2
	 *
	 * @param parameterName
	 * @param parameterValue
	 */
	public void bindParameter(ReportParameter<?> param, final String parameterName)
	{
		// ReportChooser is not actually a report parameter
		if (!(param instanceof ReportChooser))
		{

			// specific work around for prefixed report parameters
			String strippedParameterName = parameterName;
			if (strippedParameterName.startsWith("ReportParameter"))
			{
				strippedParameterName = strippedParameterName.substring("ReportParameter".length(),
						strippedParameterName.length());
			}

			if (!paramExists(strippedParameterName))
			{
				logger.info("The passed Jasper Report parameter: " + param.getParameterNames()
						+ " does not exist in the Report");
			}

			boundParams.put(strippedParameterName, param.getValue(parameterName));

		}
	}

	// /**
	// * Binds a value to a report parameter.
	// *
	// * Essentially a report can have a no. of named parameters which are used
	// to
	// * filter the report or display on the report. This method allows you to
	// * pass in a map (name, value) of parameter value at runtime.
	// *
	// * @param parameters
	// * a map of name/value pairs to bind to report parameters of the
	// * given names.
	// */
	//
	// private void bindParameters(Map<String, Object> parameters)
	// {
	// for (String parameterName : parameters.keySet())
	// {
	// Preconditions.checkArgument(paramExists(parameterName),
	// "The passed Jasper Report parameter: "
	// + parameterName + " does not existing on the Report");
	//
	// boundParams.put(parameterName, parameters.get(parameterName));
	// }
	// }

	private JasperPrint fillReport(OutputFormat outputFormat) throws JRException
	{
		Connection connection = reportProperties.getConnection();

		fillHandle = CustomAsynchronousFillHandle.createCustomHandle(jasperReport, boundParams, connection);

		fillHandle.setDataProvider(reportProperties, outputFormat);

		fillHandle.addFillListener(new FillListener()
		{

			@Override
			public void pageUpdated(JasperPrint jasperPrint, int pageIndex)
			{
				queueEntry.setStatus("Filling page " + pageIndex);
			}

			@Override
			public void pageGenerated(JasperPrint jasperPrint, int pageIndex)
			{
				queueEntry.setStatus("Generating page " + pageIndex);
			}
		});

		AsyncJasperPrintAccessor asyncAccessor = new AsyncJasperPrintAccessor(fillHandle);

		if (!stop)
		{
			fillHandle.startFill();

		}

		// jasper_print = JasperFillManager.fillReport(jasperReport,
		// boundParams, connection);
		while (asyncAccessor.getReportStatus().getStatus() == ReportExecutionStatus.Status.RUNNING)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
			}
		}

		return asyncAccessor.getFinalJasperPrint();

	}

	public void cancelPrint()
	{
		stop = true;
		if (queueEntry != null)
		{
			queueEntry.setCancelling();
			thread.interrupt();
		}

	}

	private PipedInputStream inputStream;

	private PipedOutputStream outputStream;

	private OutputFormat exportMethod;

	CountDownLatch completeBarrier;
	Map<String, byte[]> images;

	private Collection<ReportParameter<?>> params;

	private DataSource[] imagesrcs;

	private JasperProgressListener progressListener;

	private CountDownLatch readerReady;

	private CountDownLatch writerReady;

	volatile private QueueEntry queueEntry;

	private Thread thread;

	volatile private boolean inQueue;

	public RenderedReport export(OutputFormat exportMethod, Collection<ReportParameter<?>> params)
			throws InterruptedException
	{

		final CountDownLatch latch = new CountDownLatch(1);
		exportAsync(exportMethod, params, new JasperProgressListener()
		{
			
			@Override
			public void outputStreamReady()
			{
				latch.countDown();
				
			}
			
			@Override
			public void failed(String string)
			{
				latch.countDown();
				
			}
			
			@Override
			public void completed()
			{
				latch.countDown();
				
			}
		});
		latch.await();
		InputStream stream = getStream();
		// completeBarrier.await();
		return new RenderedReport(stream, imagesrcs, exportMethod);

	}

	public int checkQueueSize()
	{
		return concurrentLimit.getQueueLength();
	}

	public InputStream getStream() throws InterruptedException
	{
		inputStream = new PipedInputStream();
		readerReady.countDown();
		if (!writerReady.await(10, TimeUnit.SECONDS))
		{
			throw new RuntimeException("Couldn't attach to writer stream");
		}
		return inputStream;
	}

	public void exportAsync(OutputFormat exportMethod, Collection<ReportParameter<?>> params,
			JasperProgressListener progressListener)
	{

		if (params == null)
		{
			params = new LinkedList<ReportParameter<?>>();
		}
		this.params = params;

		images = new ConcurrentHashMap<String, byte[]>();

		if (UI.getCurrent() != null)
		{
			WrappedSession session = UI.getCurrent().getSession().getSession();
			session.setAttribute(VaadinJasperPrintServlet.IMAGES_MAP, images);
		}
		else
		{
			logger.warn("No vaadin UI present");
		}

		stop = false;
		writerReady = new CountDownLatch(1);
		completeBarrier = new CountDownLatch(1);
		readerReady = new CountDownLatch(1);
		this.progressListener = progressListener;
		if (progressListener == null)
		{
			this.progressListener = new JasperProgressListener()
			{

				@Override
				public void failed(String string)
				{
					// noop

				}

				@Override
				public void completed()
				{
					// noop

				}

				@Override
				public void outputStreamReady()
				{
					// noop

				}
			};
		}
		inputStream = null;
		outputStream = null;

		queueEntry = new QueueEntry(reportProperties.getReportTitle(), reportProperties.getUsername());
		inQueue = true;
		jobQueue.add(queueEntry);

		this.exportMethod = exportMethod;
		thread = new Thread(this, "JasperManager");
		thread.start();
	}

	static final LinkedBlockingQueue<QueueEntry> jobQueue = new LinkedBlockingQueue<QueueEntry>();

	@Override
	public void run()
	{

		JRSwapFileVirtualizer fileVirtualizer = null;
		CleanupCallback cleanupCallback = null;
		boolean initialized = false;
		try
		{
			concurrentLimit.acquire();
			initialized = true;
			inQueue = false;

			queueEntry.setStatus("Gathering report data phase 1");

			reportProperties.initDBConnection();

			cleanupCallback = reportProperties.getCleanupCallback();
			List<ReportParameter<?>> extraParams = reportProperties.prepareData(params,
					reportProperties.getReportFileName(), cleanupCallback);

			compileReport();

			if (reportProperties.getCustomReportParameterMap() != null)
			{
				boundParams.putAll(reportProperties.getCustomReportParameterMap());
			}

			if (extraParams != null)
			{
				params.removeAll(extraParams);
				params.addAll(extraParams);
			}

			logger.info("Running report " + reportProperties.getReportFileName());
			for (ReportParameter<?> param : params)
			{
				for (String parameterName : param.getParameterNames())
				{
					bindParameter(param, parameterName);
					if (param.displayInreport())
					{
						// populate dynamically added parameters to display user
						// friendly parameters on the report
						boundParams.put("ParamDisplay-" + parameterName, param.getDisplayValue(parameterName));
					}
					logger.info(parameterName + " " + param.getValue(parameterName));
				}
			}

			reportProperties.prepareForOutputFormat(exportMethod);
			CustomJRHyperlinkProducerFactory.setUseCustomHyperLinks(true);

			JRAbstractExporter exporter = null;

			queueEntry.setStatus("Gathering report data phase 2");

			// use file virtualizer to prevent out of heap
			String fileName = "/tmp";
			JRSwapFile file = new JRSwapFile(fileName, 100, 10);
			fileVirtualizer = new JRSwapFileVirtualizer(500, file);
			boundParams.put(JRParameter.REPORT_VIRTUALIZER, fileVirtualizer);

			if (stop)
			{
				return;
			}

			if (exportMethod == OutputFormat.CSV)
			{
				boundParams.put(JRParameter.IS_IGNORE_PAGINATION, true);
			}

			JasperPrint jasper_print = fillReport(exportMethod);

			switch (exportMethod)
			{
				case HTML:
				{
					exporter = new JRHtmlExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
					exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, images);

					if (VaadinServlet.getCurrent() != null)
					{
						String context = VaadinServlet.getCurrent().getServletContext().getContextPath();
						int contextIndex = Page.getCurrent().getLocation().toString().lastIndexOf(context);
						String baseurl = Page.getCurrent().getLocation().toString().substring(0,
								contextIndex + context.length() + 1);

						String imageUrl = baseurl + "VaadinJasperPrintServlet?image=";

						exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, imageUrl);
					}
					else
					{
						logger.warn("Vaadin Servlet doens't have a current context");
					}
					break;
				}
				case PDF:
				{
					exporter = new JRPdfExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
					break;
				}
				case CSV:
				{
					exporter = new JRCsvExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
					break;
				}
				default:
				{
					throw new RuntimeException("Unsupported export option " + exportMethod);
				}

			}

			imagesrcs = (images.size() <= 0) ? null : new DataSource[images.size()];
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

			if (stop)
			{
				return;
			}
			createPageProgressMonitor(exporter);

			queueEntry.setStatus("Waiting for browser to start streaming");
			progressListener.outputStreamReady();
			if (readerReady.await(10, TimeUnit.SECONDS))
			{
				outputStream = new PipedOutputStream(inputStream);
				writerReady.countDown();
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
				exporter.exportReport();
			}
			else
			{
				logger.error("Couldn't attach to reader stream");
			}
			Thread.sleep(750);
			queueEntry.setStatus("Cleaning up");

		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		finally
		{
			if (queueEntry != null)
			{
				jobQueue.remove(queueEntry);
				queueEntry = null;
			}
			try
			{
				if (outputStream != null)
				{
					outputStream.close();
				}
			}
			catch (IOException e)
			{
				logger.error(e, e);
			}

			if (cleanupCallback != null)
			{
				try
				{
					cleanupCallback.cleanup();
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
			if (fileVirtualizer != null)
			{
				try
				{
					fileVirtualizer.cleanup();
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
			CustomJRHyperlinkProducerFactory.setUseCustomHyperLinks(false);
			if (initialized)
			{
				concurrentLimit.release();
				try
				{
					reportProperties.closeDBConnection();
				}
				catch (Exception e)
				{
					logger.error(e, e);
				}
			}
			completeBarrier.countDown();
			if (progressListener != null)
			{
				progressListener.completed();
			}
		}

	}

	private void createPageProgressMonitor(JRAbstractExporter exporter)
	{
		exporter.setParameter(JRExporterParameter.PROGRESS_MONITOR, new JRExportProgressMonitor()
		{
			int pageCount = 0;

			@Override
			public void afterPageExport()
			{
				pageCount++;
				queueEntry.setStatus("Rendering page " + pageCount);

			}
		});
	}

	protected static String getJasperFile(final String dir, final String name) throws IOException
	{
		final File file = new File(dir, name);
		if (!file.exists())
			throw new FileNotFoundException(file.toString());
		return file.getCanonicalPath();
	}

	public ReportStatus getStatus()
	{
		ReportStatus reportStatus = new ReportStatus();
		try
		{
			if (queueEntry == null)
			{
				reportStatus.setStatus("Complete");
			}
			else
			{
				reportStatus.setStatus(queueEntry.getStatus());
			}
			if (inQueue)
			{

				int pos = 0;
				int ctr = 0;
				for (QueueEntry entry : jobQueue)
				{
					ctr++;
					if (entry == queueEntry)
					{
						// status += "<b>" + entry + "</b><br>";
						pos = ctr;
						break;
					}

					reportStatus.addQueueEntry(entry);

				}
				if (pos > reportLimit)
					reportStatus.setStatus("Waiting for " + (pos - reportLimit) + " of the queued reports to complete");
			}
		}
		catch (Exception e)
		{
			// there are possible race conditions that could lead to queueEntry
			// being null here, so just catch and log it. this state should be
			// transient
			reportStatus.setStatus("waiting...");
			logger.error(e, e);
		}
		return reportStatus;
	}

}
