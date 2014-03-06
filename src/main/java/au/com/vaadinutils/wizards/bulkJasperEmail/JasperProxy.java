package au.com.vaadinutils.wizards.bulkJasperEmail;

import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRException;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.JasperSettings;

public class JasperProxy
{

	private String subject;
	private String senderEmailAddress;
	private JasperManager manager;
	@SuppressWarnings("unused")
	private JasperSettings settings;
	private JasperEmailSettings emailSettings;

	public JasperProxy(String reportName, String subject, String senderEmailAddress, JasperSettings settings
			, JasperEmailSettings emailSettings) throws JRException
	{
		EntityManager em = EntityManagerProvider.createEntityManager();
		this.settings = settings;
		this.emailSettings = emailSettings;
		this.manager = new JasperManager(em, reportName, settings);
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
