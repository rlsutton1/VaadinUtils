package au.com.vaadinutils.validator;

public class EmailAndDomainValidator extends EmailValidator
{

	private static final long serialVersionUID = 1L;
	private String domain;
	private String domainErrorMessage;

	public EmailAndDomainValidator(String errorMessage, String domain, String domainErrorMessage)
	{
		super(errorMessage);
		this.domain = domain;
		this.domainErrorMessage = domainErrorMessage;
	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		super.validate(value);

		if (value != null)
		{
			String domainSubstring = value.toString().substring(value.toString().lastIndexOf('@') + 1);
			if (!domain.equalsIgnoreCase(domainSubstring))
			{
				throw new InvalidValueException(domainErrorMessage);
			}
		}
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public String getDomainErrorMessage()
	{
		return domainErrorMessage;
	}

	public void setDomainErrorMessage(String domainErrorMessage)
	{
		this.domainErrorMessage = domainErrorMessage;
	}

}
