package au.com.vaadinutils.validator;

import com.vaadin.data.Validator;

public class EmailValidator implements Validator
{
	private static final long serialVersionUID = 1L;
	private String errorMessage;

	public EmailValidator(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		org.apache.commons.validator.routines.EmailValidator validator = org.apache.commons.validator.routines.EmailValidator.getInstance();
		
		if (value != null && ((String)value).trim().length() > 0 && !validator.isValid((String) value))
			throw new InvalidValueException(errorMessage);
		
	}

}


