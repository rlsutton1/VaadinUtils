package au.com.vaadinutils.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements a daemon thread which periodically checks to see if
 * there are any threads deadlocked, and reports the situation if there are.
 * Note: this class calls System.exit(1) if it finds a deadlock.
 * 
 * @author Eric Kolotyluk
 */
public enum DeadlockFinder implements Runnable
{
	SINGLETON;

	private static final int TEST_INTERVAL = 20000;

	private static final Logger LOG = LogManager.getLogger(DeadlockFinder.class);

	private long count;

	ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	private Thread finder = null;

	public void start()
	{
		if (finder == null)
		{
			finder = new Thread(this);
			finder.setDaemon(true);
			finder.setName("DeadlockFinder"); //$NON-NLS-1$
			finder.setPriority(Thread.MIN_PRIORITY + 1);
			finder.start();
		}
	}

	public void run()
	{
		LOG.info("running, priority = " + finder.getPriority()); //$NON-NLS-1$
		try
		{
			for (;;)
			{
				Thread.sleep(TEST_INTERVAL);
				// Note: the following can be an expensive operation
				long[] threads = threadMXBean.findMonitorDeadlockedThreads();

				if (threads == null || threads.length == 0)
				{
					// Try not to flood the log with these messages.EK
					if (count++ < 100 || count % 100 == 0)
						LOG.debug("no threads are deadlocked"); //$NON-NLS-1$
					continue;
				}

				LOG.fatal(threads.length + " threads deadlocked"); //$NON-NLS-1$

				ThreadInfo[] threadInfoArray = threadMXBean.getThreadInfo(threads, Integer.MAX_VALUE);

				for (ThreadInfo threadInfo : threadInfoArray)
				{

					LOG.error("\n----------------------------"); //$NON-NLS-1$
					LOG.error("ThreadName    = " + threadInfo.getThreadName()); //$NON-NLS-1$
					LOG.error("LockName      = " + threadInfo.getLockName()); //$NON-NLS-1$
					LOG.error("LockOwnerName = " + threadInfo.getLockOwnerName()); //$NON-NLS-1$
					StackTraceElement[] stackTrace = threadInfo.getStackTrace();
					if (stackTrace == null || stackTrace.length == 0)
						LOG.error("no stack trace"); //$NON-NLS-1$
					else
					{
						LOG.error("stack trace..."); //$NON-NLS-1$
						for (StackTraceElement stackTraceElement : stackTrace)
						{

							LOG.error(stackTraceElement.toString());
						}
					}
				}

				// If we get here we have deadlocked threads so shutdown having
				// already logged the problem.
				System.exit(1);
			}
		}
		catch (InterruptedException e)
		{
			// just return
		}
	}
}
