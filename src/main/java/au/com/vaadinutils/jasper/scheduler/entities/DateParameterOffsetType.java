package au.com.vaadinutils.jasper.scheduler.entities;

import java.util.Date;

import org.joda.time.DateTime;

public enum DateParameterOffsetType
{
	CONSTANT("Select date")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			return parameterTime;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			return parameterTime;
		}
	},

	TODAY("Current day")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);
			if (formatType == DateParameterType.DATE)
			{
				result = scheduled.withTimeAtStartOfDay().toDate();
			}
			else
			{
				result = scheduled.withTimeAtStartOfDay().plusMinutes(new DateTime(parameterTime).getMinuteOfDay())
						.toDate();

			}
			return result;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);
			if (formatType == DateParameterType.DATE)
			{
				result = scheduled.withTimeAtStartOfDay().plusDays(1).toDate();
			}
			else
			{
				result = scheduled.withTimeAtStartOfDay().plusDays(1)
						.plusMinutes(new DateTime(parameterTime).getMinuteOfDay()).toDate();

			}
			return result;
		}
	},
	YESTERDAY("Previous day")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);
			if (formatType == DateParameterType.DATE)
			{
				result = scheduled.withTimeAtStartOfDay().minusDays(1).toDate();
			}
			else
			{
				result = scheduled.withTimeAtStartOfDay().minusDays(1)
						.plusMinutes(new DateTime(parameterTime).getMinuteOfDay()).toDate();

			}
			return result;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);
			if (formatType == DateParameterType.DATE)
			{
				result = scheduled.withTimeAtStartOfDay().toDate();
			}
			else
			{
				result = scheduled.withTimeAtStartOfDay().plusMinutes(new DateTime(parameterTime).getMinuteOfDay())
						.toDate();

			}
			return result;
		}
	},
	THIS_WEEK("Current week")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().toDate();

			return result;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().plusWeeks(1).toDate();

			return result;
		}
	},
	LAST_WEEK("Previous week")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().plusWeeks(-1).toDate();

			return result;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().toDate();

			return result;
		}
	},
	THIS_MONTH("Current month")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().toDate();

			return result;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().plusMonths(1).toDate();

			return result;
		}
	},
	LAST_MONTH("Previous month")
	{

		@Override
		public Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().minusMonths(1).toDate();

			return result;
		}

		@Override
		public Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().toDate();

			return result;
		}
	};

	String name;

	DateParameterOffsetType(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public abstract Date convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType);

	public abstract Date convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType);

}
