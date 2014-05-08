package au.com.vaadinutils.jasper.scheduler.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

public enum DateParameterOffsetType
{
	CONSTANT
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(parameterTime);
		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(parameterTime);
		}
	},

	TODAY
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
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
			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);

		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
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
			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}
	},
	YESTERDAY
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
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
			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
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
			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}
	},
	THIS_WEEK
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().plusWeeks(1).toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}
	},
	LAST_WEEK
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().plusWeeks(-1).toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfWeek(1).withTimeAtStartOfDay().toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}
	},
	THIS_MONTH
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().plusMonths(1).toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}
	},
	LAST_MONTH
	{
		@Override
		public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().minusMonths(1).toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}

		@Override
		public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType)
		{
			Date result;
			DateTime scheduled = new DateTime(scheduledDate);

			result = scheduled.withDayOfMonth(1).withTimeAtStartOfDay().toDate();

			SimpleDateFormat formatter = new SimpleDateFormat(formatType.getDateFormat());
			return formatter.format(result);
		}
	};

	abstract public String convertStartDate(Date parameterTime, Date scheduledDate, DateParameterType formatType);

	abstract public String convertEndDate(Date parameterTime, Date scheduledDate, DateParameterType formatType);

}
