package au.com.vaadinutils.dao;

import java.util.concurrent.atomic.AtomicInteger;


public class JpaEntityHelper
{

	 final static AtomicInteger seeds = new AtomicInteger();
	
	public static String getGuid(Object clazz)
	{
		String className = clazz.getClass().getSimpleName();
		long stamp = System.currentTimeMillis();
		long id = seeds.incrementAndGet();
		
		int current = seeds.get();
		if (current> 1000)
		{
			seeds.compareAndSet(current, 0);
		}
		return (stamp+"-"+id);
		
	}

}
