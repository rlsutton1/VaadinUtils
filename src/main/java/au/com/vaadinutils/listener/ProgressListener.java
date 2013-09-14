package au.com.vaadinutils.listener;


/**
 * Used as a generic method to track progress of a job/task/thread.
 * 
 * 
 * @author bsutton
 *
 */
public interface ProgressListener<T>
{
	/**
	 * Count and max can be used to indicate the progress towards completion.
	 * If the max number of steps is unknown max should be set to -1.
	 * 
	 * @param count
	 * @param max
	 */
	void progress(int count, int max, T status);
	
	/**
	 * Called when the job is complete.
	 */
	void complete(int sent);

	/**
	 * Used to flag that an error occurred during a transmission.
	 * @param e
	 * @param transmission
	 */
	void itemError(Exception e, T status);

	
	/**
	 * An unrecoverable error occurred during transmission.
	 * @param e
	 */
	void exception(Exception e);

}
