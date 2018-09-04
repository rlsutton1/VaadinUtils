package au.com.vaadinutils.dao;

public interface AutoCloseableEM extends AutoCloseable
{

	@Override
	void close();
}
