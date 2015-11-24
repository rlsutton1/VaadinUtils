package au.com.vaadinutils.validator.numberRange;

import com.vaadin.data.validator.AbstractValidator;

public abstract class RangeValidator<E extends Number> extends AbstractValidator<E>
{

	private static final long serialVersionUID = 1L;

	protected Class<E> type;
	protected String parseErrorMessage;
	protected String rangeErrorMessage;
	protected E minValue;
	protected E maxValue;
	protected Boolean minInclusive;
	protected Boolean maxInclusive;

	public RangeValidator(Class<E> type, String parseErrorMessage, String rangeErrorMessage, E minValue, E maxValue,
			Boolean minInclusive, Boolean maxInclusive)
	{
		super(rangeErrorMessage);

		this.type = type;
		this.parseErrorMessage = parseErrorMessage;
		this.rangeErrorMessage = rangeErrorMessage;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}

	@Override
	protected boolean isValidValue(E num)
	{
		boolean isValid = false;

		if (greaterThanMin(num) && lessThanMax(num))
		{
			isValid = true;
		}

		return isValid;
	}

	@Override
	public Class<E> getType()
	{
		return type;
	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null)
		{
			// get numeric value of String
			E num = getObjectValue(value.toString());

			// check if within the specified range
			if (!isValidValue(num))
			{
				throw new InvalidValueException(rangeErrorMessage);
			}
		}
	}

	protected abstract E getObjectValue(String str);

	// minimum value checker
	protected abstract boolean greaterThanMin(E num);

	// maximum value checker
	protected abstract boolean lessThanMax(E num);

}
