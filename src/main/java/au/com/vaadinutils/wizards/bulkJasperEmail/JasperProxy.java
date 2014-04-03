package au.com.vaadinutils.wizards.bulkJasperEmail;

import javax.persistence.EntityManager;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.ui.ReportProperties;

public class JasperProxy
{

	private String subject;
	private String senderEmailAddress;
	private JasperEmailSettings emailSettings;
	private JasperManager manager;

	public JasperProxy(String subject, String senderEmailAddress, JasperEmailSettings emailSettings,
			ReportProperties reportProperties)
	{
		EntityManager em = EntityManagerProvider.createEntityManager();
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
