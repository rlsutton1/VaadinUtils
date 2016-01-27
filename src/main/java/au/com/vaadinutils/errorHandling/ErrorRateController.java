package au.com.vaadinutils.errorHandling;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class ErrorRateController
{

	final LinkedBlockingQueue<Boolean> availablePermits = new LinkedBlockingQueue<>();

	final Stopwatch lastCreated = Stopwatch.createStarted();

	private long maxBurst;

	private double permitRate;

	private TimeUnit permitRateUnits;

	/**
	 * 
	 * @param maxBurst
	 *            - maximum of permits that will be stockpiled for a burst after
	 *            a period of less or activity
	 * @param permitRate
	 *            - rate at which permits become available per permitRateUnits
	 * @param permitRateUnits
	 */
	ErrorRateController(long maxBurst, double permitRate, TimeUnit permitRateUnits)
	{
		this.maxBurst = maxBurst;
		this.permitRate = permitRate;
		this.permitRateUnits = permitRateUnits;

		// fill the availablePermits
		for (int i = 0; i < maxBurst; i++)
		{
			availablePermits.add(true);
		}

	}

	/**
	 * returns true if able to acquire a permit, more specifically true if the rate has not been exceeded.
	 * @return
	 */
	public boolean acquire()
	{
		Boolean permit = availablePermits.poll();
		if (permit == null)
		{
			long permitsToAdd = 0;
			synchronized (lastCreated)
			{
				permitsToAdd = Math.min(maxBurst, (long) (lastCreated.elapsed(permitRateUnits) * permitRate));
				if (permitsToAdd > 0)
				{
					lastCreated.reset();
					lastCreated.start();
					permit = true;
				}

			}
			for (int i = 0; i < permitsToAdd-1; i++)
			{
				availablePermits.add(true);
			}
//			if (permitsToAdd > 0)
//			{
//				logger.info("Added " + permitsToAdd);
//			}
			
		}

		return permit != null;
	}
}
