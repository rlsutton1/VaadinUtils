package au.com.vaadinutils.jasper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.jasperreports.crosstabs.JRCrosstab;
import net.sf.jasperreports.engine.JRBreak;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JREllipse;
import net.sf.jasperreports.engine.JRFrame;
import net.sf.jasperreports.engine.JRGenericElement;
import net.sf.jasperreports.engine.JRImage;
import net.sf.jasperreports.engine.JRLine;
import net.sf.jasperreports.engine.JRRectangle;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JRVisitor;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JasperReportCompiler
{
	Logger logger = LogManager.getLogger();
	protected Throwable subReportException;

	public JasperReport compileReportIfNeeded(JasperDesign jasperDesign, final File sourcePath, final File outputPath,
			String reportName) throws Throwable
	{
		JasperReport report;
		if (checkIfReportNeedsCompile(sourcePath, outputPath, reportName))
		{
			report = compileReport(jasperDesign, sourcePath, outputPath, reportName);
	 	}
		else
		{
			File outputReport = new File(outputPath.getAbsolutePath() + "/" + reportName + ".jasper");

			logger.warn("Report " + outputReport.getName() + " is up to date");
			report = (JasperReport) JRLoader.loadObject(outputReport);

		}
		return report;
	}

	public JasperDesign getDesignFile(final File sourcePath, String reportName) throws Throwable
	{
		String name = reportName;
		if (!name.contains(".jrxml"))
		{
			name = name+".jrxml";
		}
		File sourceReport = new File(sourcePath.getAbsoluteFile() + "/" + name);
		return JRXmlLoader.load(sourceReport);

	}

	public JasperReport compileReport( final File sourcePath, final File outputPath,
			String reportName) throws Throwable
	{
		String name = reportName;
		if (!name.contains(".jasper"))
		{
			name = name+".jasper";
		}
		JasperReport jasperReport = null;
		JasperDesign jasperDesign = getDesignFile(sourcePath, reportName);
		File outputReport = new File(outputPath.getAbsolutePath() + "/" + name);
		jasperReport = JasperCompileManager.compileReport(jasperDesign);
		JRSaver.saveObject(jasperReport, outputReport);
		logger.warn("Saving compiled report : " + outputReport.getName());

		// Compile sub reports
		JRElementsVisitor.visitReport(jasperReport, createVisitor(sourcePath, outputPath));

		if (subReportException != null)
			throw new RuntimeException(subReportException);
		return jasperReport;
	}
	
	public JasperReport compileReport(JasperDesign jasperDesign, final File sourcePath, final File outputPath,
			String reportName) throws Throwable
	{
		JasperReport jasperReport = null;
		File outputReport = new File(outputPath.getAbsolutePath() + "/" + reportName + ".jasper");
		jasperReport = JasperCompileManager.compileReport(jasperDesign);
		JRSaver.saveObject(jasperReport, outputReport);
		logger.warn("Saving compiled report : " + outputReport.getAbsolutePath());

		// Compile sub reports
		JRElementsVisitor.visitReport(jasperReport, createVisitor(sourcePath, outputPath));

		if (subReportException != null)
			throw new RuntimeException(subReportException);
		return jasperReport;
	}

	public boolean checkIfReportNeedsCompile(final File sourcePath, final File outputPath, String reportName)
	{
		File sourceReport = new File(sourcePath.getAbsoluteFile() + "/" + reportName + ".jrxml");
		File outputReport = new File(outputPath.getAbsolutePath() + "/" + reportName + ".jasper");
		return !outputReport.exists() || sourceReport.lastModified() > outputReport.lastModified();
	}

	private JRVisitor createVisitor(final File sourcePath, final File outputPath)
	{
		return new JRVisitor()
		{
			private Set<String> completedSubReports = new HashSet<String>();

			@Override
			public void visitBreak(JRBreak breakElement)
			{
			}

			@Override
			public void visitChart(JRChart chart)
			{
			}

			@Override
			public void visitCrosstab(JRCrosstab crosstab)
			{
			}

			@Override
			public void visitElementGroup(JRElementGroup elementGroup)
			{
			}

			@Override
			public void visitEllipse(JREllipse ellipse)
			{
			}

			@Override
			public void visitFrame(JRFrame frame)
			{
			}

			@Override
			public void visitImage(JRImage image)
			{
			}

			@Override
			public void visitLine(JRLine line)
			{
			}

			@Override
			public void visitRectangle(JRRectangle rectangle)
			{
			}

			@Override
			public void visitStaticText(JRStaticText staticText)
			{
			}

			@Override
			public void visitSubreport(JRSubreport subreport)
			{
				try
				{
					String expression = subreport.getExpression().getText().replace(".jasper", "");
					StringTokenizer st = new StringTokenizer(expression, "\"/");
					String subReportName = null;
					while (st.hasMoreTokens())
						subReportName = st.nextToken();
					// Sometimes the same subreport can be used multiple times,
					// but
					// there is no need to compile multiple times
					if (completedSubReports.contains(subReportName))
						return;
					completedSubReports.add(subReportName);
					compileReport( sourcePath, outputPath, subReportName);
				}
				catch (Throwable e)
				{
					subReportException = e;
				}
			}

			@Override
			public void visitTextField(JRTextField textField)
			{
			}

			@Override
			public void visitComponentElement(JRComponentElement componentElement)
			{
			}

			@Override
			public void visitGenericElement(JRGenericElement element)
			{
			}
		};
	}
}
