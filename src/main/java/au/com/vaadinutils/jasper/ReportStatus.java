package au.com.vaadinutils.jasper;

import java.util.LinkedList;
import java.util.List;

public class ReportStatus
{

	List<QueueEntry> entries = new LinkedList<QueueEntry>();
	private String status;

	public void addQueueEntry(QueueEntry entry)
	{
		entries.add(entry);

	}

	public void setStatus(String string)
	{
		status = string;

	}

	/**
	 * @return the entries
	 */
	public List<QueueEntry> getEntries()
	{
		return entries;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

}
