package au.com.vaadinutils.wizards.bulkJasperEmail;


public class JasperTransmission
{
	private Recipient recipient;
	
	// If an exception is thrown during transmission it is stored here.
	private Exception exception;

	public JasperTransmission(Recipient recipient)
	{
		this.recipient = recipient;
	}

	public JasperTransmission(Recipient recipient, JasperProxy proxy, Exception exception)
	{
		this.recipient = recipient;
		this.exception = exception;
	}

	public String getRecipientEmailAddress()
	{
		return this.recipient.getEmailAddress();
	}

	public void setException(Exception e)
	{
		this.exception = e;

	}

	public String getException()
	{
		if (exception != null)
			return exception.getClass().getSimpleName() + ": " + exception.getMessage();
		else
			return "Success";
	}

	public String getDescription()
	{
		return recipient.getDescription();
	}


}
