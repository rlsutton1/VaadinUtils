package au.com.vaadinutils.jasper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QueueEntry
{
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	final private String title;
	final Date requestTime = new Date();
	private boolean cancelling = false;

	final private String user;

	volatile private String status;

	public QueueEntry(String reportTitle, String username)
	{
		title = reportTitle;
		user = username;

	}

	@Override
	public String toString()
	{
		String details = sdf.format(requestTime) + " " + title + " <b>" + user + "</b>";
		if (cancelling)
		{
			details += " CANCELLING";
		}
		return details;
	}

	public void setCancelling()
	{
		cancelling = true;

	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getTime()
	{
		return sdf.format(requestTime);
	}

	public String getReportName()
	{
		return title;
	}

	public String getUser()
	{
		return user;
	}

	public String getStatus()
	{

		if (cancelling)
			return "Cancelling";
		return status;
	}

}
