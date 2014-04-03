package au.com.vaadinutils.jasper;

public interface JasperProgressListener
{

	
	void failed(String string);

	void completed();

	void outputStreamReady();

}
