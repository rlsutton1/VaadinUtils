package au.com.vaadinutils.jasper;

import java.io.File;
import java.util.ArrayList;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;

/**
 * Use this class to send a Jasper Report as an email.
 * @author bsutton
 *
 */
public class JasperEmailBuilder
{
	private JasperEmailSettings settings;

	private ArrayList<DataSource> attachments = new ArrayList<DataSource>();
	private ArrayList<String> tos = new ArrayList<String>();
	private ArrayList<String> ccs = new ArrayList<String>();
	private ArrayList<String> bccs = new ArrayList<String>();
	private String subject;
	private String fromAddress;

	// Different body types
	private RenderedReport renderedReportBody;
	private String htmlBody;
	private String textBody = "Your email client does not support HTML messages";

	public JasperEmailBuilder(JasperEmailSettings settings)
	{
		this.settings = settings;
	}

	public JasperEmailBuilder addTo(String toAddress)
	{
		this.tos.add(toAddress);
		return this;
	}

	public JasperEmailBuilder addCC(String ccAddress)
	{
		this.ccs.add(ccAddress);
		return this;
	}

	public JasperEmailBuilder addBCC(String bccAddress)
	{
		this.bccs.add(bccAddress);
		return this;
	}

	public JasperEmailBuilder setSubject(String subject)
	{
		this.subject = subject;
		return this;
	}

	public JasperEmailBuilder setFrom(String fromAddress)
	{
		this.fromAddress = fromAddress;
		return this;
	}

	public JasperEmailBuilder setHtmlBody(RenderedReport renderedReport)
	{
		Preconditions.checkArgument(this.htmlBody == null, "You may only call one of the setXXXBody methods.");
		Preconditions.checkArgument(renderedReport.isHTML(), "The report has not been exported to HTML.");
		this.renderedReportBody = renderedReport;
		return this;
	}

	public JasperEmailBuilder setHtmlBody(String htmlBody)
	{
		Preconditions.checkArgument(this.renderedReportBody == null,
				"You may only call one of the setXXXBody methods.");
		this.htmlBody = htmlBody;
		return this;
	}

	public JasperEmailBuilder setTextBody(String body)
	{
		this.textBody = body;
		return this;
	}

	public JasperEmailBuilder addAttachement(File attachement)
	{
		this.attachments.add(new FileDataSource(attachement));
		return this;
	}

	public JasperEmailBuilder addAttachement(DataSource attachement)
	{
		this.attachments.add(attachement);
		return this;
	}

	public void send(boolean debug) throws EmailException
	{

		Preconditions.checkNotNull(fromAddress);
		Preconditions.checkNotNull(tos.size() > 0);
		Preconditions.checkNotNull(subject);
		Preconditions.checkNotNull(this.htmlBody != null || this.renderedReportBody != null,
				"You must specify a body.");

		ImageHtmlEmail email = new ImageHtmlEmail();
		if (this.renderedReportBody != null)
			email.setDataSourceResolver(new JasperDataSourceResolver(renderedReportBody));

		email.setDebug(debug);
		email.setHostName(settings.getSmtpFQDN());
		email.setSmtpPort(settings.getSmtpPort());
		if (settings.isAuthRequired())
			email.setAuthentication(settings.getUsername(), settings.getPassword());
		if (settings.getUseSSL())
		{
			email.setSslSmtpPort(settings.getSmtpPort().toString());
			email.setSSLOnConnect(true);
			email.setSSLCheckServerIdentity(false);
		}
		email.setFrom(fromAddress);
		email.setBounceAddress(settings.getBounceEmailAddress());
		email.setSubject(subject);

		for (String to : this.tos)
			email.addTo(to);

		for (String cc : this.ccs)
			email.addCc(cc);

		for (String bcc : this.bccs)
			email.addBcc(bcc);

		if (this.htmlBody != null)
			email.setHtmlMsg(this.htmlBody);

		if (this.renderedReportBody != null)
			email.setHtmlMsg(this.renderedReportBody.getBodyAsHtml());

		email.setTextMsg(this.textBody);

		for (DataSource attachment : this.attachments)
			email.attach(attachment, attachment.getName(), attachment.getName());

		email.send();
	}
}