package au.com.vaadinutils.jasper;

import java.io.File;
import java.util.HashMap;

public interface JasperSettings
{

	String getReportDir();

	File getDocumentBase();

	File getReportFile(String reportName);

	/**
	 * 
	 * @return the name of the template jasper file (something like
	 *         "HeaderFooter") or null if your not using a template
	 */
	String getHeaderFooterTemplateName();

}
