package au.org.vaadinutil.util;


public class ProgressBarWorker<T> extends Thread
{
	private ProgressBarTask<T> task;

	public ProgressBarWorker(ProgressBarTask<T> task)
	{
		super(ProgressBarTask.class.getName());
		this.task = task;
	}

	@Override
	public void run()
	{
		task.run();
	}
}