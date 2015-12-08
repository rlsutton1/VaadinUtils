package au.com.vaadinutils.validator;

import java.util.Collection;

import com.vaadin.data.Validator;

public class EmptyCollectionValidator implements Validator
{
	private static final long serialVersionUID = 1L;

	private String errorMessage;

	public EmptyCollectionValidator(String errorMessage)
	{
		setErrorMessage(errorMessage);
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (((Collection) value).isEmpty())
		{
			throw new InvalidValueException(errorMessage);
		}
	}
}
