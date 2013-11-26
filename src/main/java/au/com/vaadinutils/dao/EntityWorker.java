package au.com.vaadinutils.dao;


public interface EntityWorker<T>
{

	T exec() throws Exception;

}
