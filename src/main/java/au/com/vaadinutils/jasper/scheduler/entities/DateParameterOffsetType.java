package au.com.vaadinutils.jasper.scheduler.entities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum DateParameterOffsetType
{
	CONSTANT
	{
		@Override
		public String convertDate(Date parameterTime, Date scheduledDate)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return formatter.format(parameterTime);
		}
	},
	DAY_OF_SCHEDULE
	{
		@Override
		public String convertDate(Date parameterTime, Date scheduledDate)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar pTime = getCalendar(parameterTime);
			Calendar sDate = getCalendar(scheduledDate);

			Calendar tmpDate = Calendar.getInstance();
			tmpDate.setTime(scheduledDate);
			tmpDate.set(Calendar.HOUR_OF_DAY, pTime.get(Calendar.HOUR_OF_DAY));
			tmpDate.set(Calendar.MINUTE, pTime.get(Calendar.MINUTE));
			return formatter.format(tmpDate.getTime());
		}
	},
	DAY_BEFORE_SCHEDULE
	{
		@Override
		public String convertDate(Date parameterTime, Date scheduledDate)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar pTime = getCalendar(parameterTime);
			Calendar sDate = getCalendar(scheduledDate);

			Calendar tmpDate = Calendar.getInstance();
			tmpDate.setTime(scheduledDate);
			tmpDate.set(Calendar.HOUR_OF_DAY, pTime.get(Calendar.HOUR_OF_DAY));
			tmpDate.set(Calendar.MINUTE, pTime.get(Calendar.MINUTE));
			tmpDate.add(Calendar.DATE, -1);
			return formatter.format(tmpDate);
		}
	},
	WEEK_BEFORE_SCHEDULE
	{
		@Override
		public String convertDate(Date parameterTime, Date scheduledDate)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar pTime = getCalendar(parameterTime);
			Calendar sDate = getCalendar(scheduledDate);

			Calendar tmpDate = Calendar.getInstance();
			tmpDate.setTime(scheduledDate);
			tmpDate.set(Calendar.HOUR_OF_DAY, pTime.get(Calendar.HOUR_OF_DAY));
			tmpDate.set(Calendar.MINUTE, pTime.get(Calendar.MINUTE));
			tmpDate.add(Calendar.WEEK_OF_YEAR, -1);
			return formatter.format(tmpDate);
		}
	},
	MONTH_BEFORE_SCHEDULE
	{
		@Override
		public String convertDate(Date parameterTime, Date scheduledDate)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar pTime = getCalendar(parameterTime);
			Calendar sDate = getCalendar(scheduledDate);

			Calendar tmpDate = Calendar.getInstance();
			tmpDate.setTime(scheduledDate);
			tmpDate.set(Calendar.HOUR_OF_DAY, pTime.get(Calendar.HOUR_OF_DAY));
			tmpDate.set(Calendar.MINUTE, pTime.get(Calendar.MINUTE));
			tmpDate.add(Calendar.MONTH, -1);
			return formatter.format(tmpDate);
		}
	};

	abstract public String convertDate(Date parameterTime, Date scheduledDate);

	private static Calendar getCalendar(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

}
