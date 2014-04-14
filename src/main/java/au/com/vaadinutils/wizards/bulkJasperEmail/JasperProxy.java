package au.com.vaadinutils.wizards.bulkJasperEmail;

import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

public class JasperProxy
{

	private String subject;
	private String senderEmailAddress;
	private JasperEmailSettings emailSettings;
	private JasperManager manager;

	public JasperProxy(String subject, String senderEmailAddress, JasperEmailSettings emailSettings,
			JasperReportProperties reportProperties)
	{
		this.emailSettings = emailSettings;
		this.manager = new JasperManager(reportProperties);
		this.subject = subject;
		this.senderEmailAddress = senderEmailAddress;
	}

	JasperManager getManager()
	{
		return this.manager;
	}

	public String getSubject()
	{
		return subject;
	}

	public String getSenderEmailAddress()
	{
		return senderEmailAddress;
	}

	public JasperEmailSettings getEmailSettings()
	{
		return emailSettings;
	}

}
