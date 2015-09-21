package au.com.vaadinutils.validator;

import com.vaadin.data.Validator;

public class ComboBoxSelectionValidator implements Validator
{

	private static final long serialVersionUID = 1L;

	private String errorMessage;

	public ComboBoxSelectionValidator(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public void validate(Object object) throws InvalidValueException
	{
		// validation for empty selection
		if (object == null)
		{
			throw new EmptyValueException(errorMessage);
		}
	}

}
