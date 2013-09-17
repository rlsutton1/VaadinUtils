package au.org.vaadinutil.util;

import au.com.vaadinutils.ui.UIUpdater;

public abstract class ProgressBarTask<T>
{
	private ProgressTaskListener<T> listener;

	public ProgressBarTask(ProgressTaskListener<T> listener)
	{
		this.listener = listener;
	}

	abstract public void run();

	protected void taskComplete(final int sent)
	{
		new UIUpdater(new Runnable()
		{

			@Override
			public void run()
			{
				listener.taskComplete(sent);
			}

		});

	}

	public void taskProgress(final int count, final int max, final T status)
	{
		new UIUpdater(new Runnable()
		{

			@Override
			public void run()
			{
				listener.taskProgress(count, max, status);
			}
		}

		);
	}

	public void taskItemError(final T status)
	{
		new UIUpdater(new Runnable()
		{

			@Override
			public void run()
			{
				listener.taskItemError(status);
			}
		}

		);

	}

	public void taskException(final Exception e)
	{
		new UIUpdater(new Runnable()
		{

			@Override
			public void run()
			{
				listener.taskException(e);
			}
		}

		);

	}
}
