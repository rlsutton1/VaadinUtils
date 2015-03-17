package au.com.vaadinutils.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class PipedOutputStreamWrapperTest
{

	@Test
	public void testWriterExitDirty() throws InterruptedException, TimeoutException, IOException
	{
		final PipedOutputStreamWrapper wrapper = new PipedOutputStreamWrapper();

		Runnable runner = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					wrapper.write("Test".getBytes());
				}
				catch (IOException e)
				{
					e.printStackTrace();
					fail(e.getMessage());
				}

				finally
				{
					try
					{
						wrapper.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
						fail(e.getMessage());
					}
				}

			}
		};
		new Thread(runner).start();

		final byte[] buffer = new byte[4];
		wrapper.waitForOutputToBeReady(100, TimeUnit.MILLISECONDS);

		wrapper.getInputStream().read(buffer);
		String result = new String(buffer);
		boolean equals = result.equals("Test");
		assertTrue("Expected Test got " + result, equals);

	}
	// Logger logger = LogManager.getLogger();
}
