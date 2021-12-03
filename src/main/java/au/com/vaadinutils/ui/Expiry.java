package au.com.vaadinutils.ui;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class Expiry
{
	private final Stopwatch stopwatch = Stopwatch.createStarted();
	private final TimeUnit units;
	private volatile int qty;

	public Expiry(TimeUnit units, long qty)
	{
		this.units = units;
		this.qty = (int) qty;
	}

	public boolean isNotExpired()
	{
		return stopwatch.elapsed(units) < qty;
	}

	public boolean isExpired()
	{
		return stopwatch.elapsed(units) >= qty;
	}

	public long getRamaining(TimeUnit unit)
	{
		long unitsRemail = qty - stopwatch.elapsed(units);
		return Math.max(0, unit.convert(unitsRemail, units));

	}

	public void expire()
	{
		qty = 0;
	}

}
