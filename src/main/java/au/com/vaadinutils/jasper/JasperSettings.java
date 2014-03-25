package au.com.vaadinutils.jasper;

import java.io.File;
import java.util.HashMap;

public interface JasperSettings
{

	String getReportDir();

	File getDocumentBase();

	File getReportFile(String reportName);

	// use to default to baseurl + "images?image="
	String getImageUriFormat(String baseurl);

	HashMap<String, byte[]> getNewImageMap();

}
