package au.com.vaadinutils.validator.numberRange;

public class IntegerRangeValidator extends RangeValidator<Integer>
{

	private static final long serialVersionUID = 1L;

	public IntegerRangeValidator(Class<Integer> type, String parseErrorMessage, String rangeErrorMessage,
			Integer minValue, Integer maxValue, Boolean minInclusive, Boolean maxInclusive)
	{
		super(type, parseErrorMessage, rangeErrorMessage, minValue, maxValue, minInclusive, maxInclusive);
	}

	@Override
	protected Integer getObjectValue(String str)
	{
		Integer number = 0;
		if (str != null && !"".equalsIgnoreCase(str))
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

	@Override
	protected boolean greaterThanMin(Integer num)
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

	@Override
	protected boolean lessThanMax(Integer num)
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
