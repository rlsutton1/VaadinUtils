package au.com.vaadinutils.jasper;

import java.io.File;

public interface JasperSettings
{

	String getReportDir();

	File getDocumentBase();

	File getReportFile(String reportName);

}
