package au.org.scoutmaster.views.wizards.bulkJasperEmail;

import java.io.File;

import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRException;
import au.com.vaadinutils.impl.JasperSettingsImpl;
import au.org.scoutmaster.jasper.JasperManager;

public class JasperProxy
{

	private String subject;
	private String senderEmailAddress;
	private JasperManager manager;

	public JasperProxy(File jasperReport, String subject, String senderEmailAddress) throws JRException
	{
		EntityManager em = au.com.vaadinutils.impl.LocalEntityManagerFactory.createEntityManager();
		this.manager = new JasperManager(em, jasperReport, new JasperSettingsImpl());
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



}
