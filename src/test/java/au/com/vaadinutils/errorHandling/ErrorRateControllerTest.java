package au.com.vaadinutils.errorHandling;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.base.Stopwatch;

public class ErrorRateControllerTest
{

	private static final int RATE = 5;
	private static final int BURST = 20;

	@Test
	public void test() throws InterruptedException
	{
		ErrorRateController errorRateController = new ErrorRateController(BURST, RATE, TimeUnit.SECONDS);
		Stopwatch timer = Stopwatch.createStarted();
		int counter = runRateTest(errorRateController, 100);
		final long expected = expected(timer);
		assertTrue("exptected " + expected + " got " + counter, Math.abs(counter - expected) <= RATE);

	}

	@Test
	public void testBurst() throws InterruptedException
	{
		ErrorRateController errorRateController = new ErrorRateController(BURST, RATE, TimeUnit.SECONDS);
		int counter = runRateTest(errorRateController, 0);
		Thread.sleep(2000);
		counter += runRateTest(errorRateController, 0);

		assertTrue("exptected " + (BURST + (RATE * 2)) + " got " + counter,
				Math.abs(counter - (BURST + (RATE * 2))) <= RATE);

	}

	private int runRateTest(ErrorRateController errorRateController, long delay) throws InterruptedException
	{

		int counter = 0;
		for (int i = 0; i < 60; i++)
		{
			if (errorRateController.acquire())
			{
				counter++;
				System.out.println("Got a permit " + counter);
			}
			Thread.sleep(delay);
		}
		return counter;

	}

	@Test
	public void multiThreadTest() throws InterruptedException
	{
		final ErrorRateController errorRateController = new ErrorRateController(BURST, RATE, TimeUnit.SECONDS);
		Stopwatch timer = Stopwatch.createStarted();

		final AtomicInteger count = new AtomicInteger();
		final CountDownLatch latch = new CountDownLatch(10);
		for (int i = 0; i < 10; i++)
		{
			Runnable r = new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						count.addAndGet(runRateTest(errorRateController, 100));
						latch.countDown();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

				}
			};
			new Thread(r).start();
		}
		latch.await();
		final long expected = expected(timer);
		assertTrue("exptected " + expected + " got " + count.get(), Math.abs(count.get() - expected) <= RATE);

	}

	// Logger logger = LogManager.getLogger();

	private long expected(Stopwatch timer)
	{
		return 20 + (timer.elapsed(TimeUnit.SECONDS) * RATE);
	}
}
