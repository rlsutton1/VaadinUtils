package au.com.vaadinutils.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * encapsulates the process of joining together a PipedInputStream and a
 * PipedOutputStream, without leaving either of them hanging
 * 
 * @author rsutton
 *
 */
public class PipedOutputStreamWrapper extends OutputStream
{

	final CountDownLatch readLatch = new CountDownLatch(1);
	final CountDownLatch writeLatch = new CountDownLatch(1);

	final private PipedInputStream inputStream = new PipedInputStream();
	volatile private PipedOutputStream outputStream;

	volatile Long writerThreadId;

	Logger logger = LogManager.getLogger();

	/**
	 * do not call this method until outputIsReady returns true
	 * 
	 * the thread that calls this method must NOT be the same thread that is
	 * writing to the OutputStream
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public InputStream getInputStream() throws InterruptedException
	{
		if (!isOutputReady())
		{
			Exception e = new Exception(
					"Request for InputStream before the writer is ready, "
							+ " you should ensure the writer is ready by calling outputIsReady() or waitForOutputToBeReady() first.");
			logger.error(e, e);
		}
		Preconditions.checkState(writerThreadId != Thread.currentThread().getId(),
				"The consumer thread(getInputStream) must not be the producer thread(write)");

		if (!writeLatch.await(10, TimeUnit.MINUTES))
		{
			throw new RuntimeException("Reader timeout, waiting for writer");
		}
		readLatch.countDown();
		return inputStream;
	}

	@Override
	public void write(int b) throws IOException
	{
		if (writeLatch.getCount() > 0)
		{
			writerThreadId = Thread.currentThread().getId();
			writeLatch.countDown();
			outputStream = new PipedOutputStream(inputStream);
			try
			{
				if (!readLatch.await(10, TimeUnit.MINUTES))
				{
					throw new RuntimeException("Writer timeout, waiting for reader.");
				}
			}
			catch (InterruptedException e)
			{
				outputStream.close();
				inputStream.close();
				throw new RuntimeException(e);
			}
		}

		outputStream.write(b);

	}

	public boolean isOutputReady()
	{
		return writeLatch.getCount() == 0;
	}

	public void close() throws IOException
	{
		if (outputStream != null)
		{
			outputStream.close();
		}
	}

	public void waitForOutputToBeReady(int duration, TimeUnit unit) throws InterruptedException, TimeoutException
	{
		if (!writeLatch.await(duration, unit))
		{
			throw new TimeoutException("Output stream Timeout");
		}

	}

}
