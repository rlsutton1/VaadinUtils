package au.com.vaadinutils.validator;

import com.vaadin.data.Validator;

public class IntegerRangeValidator implements Validator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String parseErrorMessage;
	private String rangeErrorMessage;
	private Integer minValue;
	private Integer maxValue;
	private Boolean minInclusive;
	private Boolean maxInclusive;

	public IntegerRangeValidator(String parseErrorMessage, String rangeErrorMessage, Integer minValue,
			Integer maxValue, Boolean minInclusive, Boolean maxInclusive)
	{
		this.parseErrorMessage = parseErrorMessage;
		this.rangeErrorMessage = rangeErrorMessage;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null)
		{
			// get numeric value of String
			Integer num = getIntegerValue(value.toString());

			// check if within the specified range
			if (!isWithinRange(num))
			{
				throw new InvalidValueException(rangeErrorMessage);
			}
		}
	}

	private Integer getIntegerValue(String str)
	{
		Integer number = 0;
		if (str != null)
		{
			try
			{
				number = Integer.parseInt(str);
			}
			catch (Exception e)
			{
				throw new InvalidValueException(parseErrorMessage);
			}
		}
		return number;
	}

	private boolean isWithinRange(Integer num)
	{
		boolean isValid = false;

		if (greaterThanMin(num) && lessThanMax(num))
		{
			isValid = true;
		}

		return isValid;
	}

	// minimum value checker
	private boolean greaterThanMin(Integer num)
	{
		boolean isHigher = false;
		if (minValue != null)
		{
			if (minInclusive)
			{
				isHigher = (num >= minValue);
			}
			else
			{
				isHigher = (num > minValue);
			}
		}
		return isHigher;
	}

	// maximum value checker
	private boolean lessThanMax(Integer num)
	{
		boolean isLower = false;
		if (maxValue != null)
		{
			if (maxInclusive)
			{
				isLower = (num <= maxValue);
			}
			else
			{
				isLower = (num < maxValue);
			}
		}
		return isLower;
	}

}
