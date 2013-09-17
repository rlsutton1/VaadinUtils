package au.com.vaadinutils.wizards.bulkJasperEmail;


public class RecipientException extends Exception
{
	private static final long serialVersionUID = 1L;
	private Recipient recipient;
	private String cause;

	RecipientException(String cause, Recipient recipient)
	{
		this.recipient = recipient;
		this.cause = cause;
	}
	
	public String getMessage()
	{
		return recipient.getDescription()
				+ " rejected due to : " + cause;
	}
	
}
