package au.com.vaadinutils.validator.numberRange;

public class DoubleRangeValidator extends RangeValidator<Double>
{

	private static final long serialVersionUID = 1L;

	public DoubleRangeValidator(Class<Double> type, String parseErrorMessage, String rangeErrorMessage,
			Double minValue, Double maxValue, Boolean minInclusive, Boolean maxInclusive)
	{
		super(type, parseErrorMessage, rangeErrorMessage, minValue, maxValue, minInclusive, maxInclusive);
	}

	@Override
	protected Double getObjectValue(String str)
	{
		Double number = 0.0;
		if (str != null)
		{
			try
			{
				number = Double.parseDouble(str);
			}
			catch (Exception e)
			{
				throw new InvalidValueException(parseErrorMessage);
			}
		}
		return number;
	}

	@Override
	protected boolean greaterThanMin(Double num)
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
	protected boolean lessThanMax(Double num)
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
