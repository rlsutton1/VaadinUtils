package au.com.vaadinutils.jasper.parameter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

import com.google.common.base.Preconditions;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.VerticalLayout;

public class ReportParameterDateTimeRange extends ReportParameter<String>
{
	protected DateField startfield;
	protected DateField endfield;

	protected String parameterFormat = "yyyy/MM/dd HH:mm:ss";
	protected String endParameterName;
	protected final String startParameterName;

	/**
	 * 
	 * @param caption
	 *            - shown on the UI
	 * @param parameterName
	 *            - parameter name passed to ireport
	 * @param resolution
	 *            - Vaadin calendar control resolution
	 * @param displayFormat
	 *            - format to display to the user
	 * @param parameterFormat
	 *            - format of the value passed to ireport
	 */
	public ReportParameterDateTimeRange(String caption, String startParameterName, String endParameterName,
			Resolution resolution, String displayFormat, String parameterFormat)
	{
		super(caption, new String[] { startParameterName, endParameterName });
		Preconditions.checkNotNull(startParameterName);
		Preconditions.checkNotNull(endParameterName);
		this.startParameterName = startParameterName;
		this.endParameterName = endParameterName;
		startfield = new DateField(caption, new DateTime().toDate());
		startfield.setResolution(resolution);
		startfield.setDateFormat(displayFormat);
		this.parameterFormat = parameterFormat;
		startfield.setImmediate(true);

		startfield.setValidationVisible(true);

		endfield = new DateField("To", new DateTime().toDate());
		endfield.setResolution(resolution);
		endfield.setDateFormat(displayFormat);
		this.parameterFormat = parameterFormat;
		endfield.setImmediate(true);

		endfield.setValidationVisible(true);
		createValidators();
	}

	public void addValueChangeListener(ValueChangeListener listener)
	{
		startfield.addValueChangeListener(listener);
		endfield.addValueChangeListener(listener);

	}

	public ReportParameterDateTimeRange(String caption, String startParameterName, String endParameterName)
	{
		super(caption, new String[] { startParameterName, endParameterName });
		Preconditions.checkNotNull(startParameterName);
		Preconditions.checkNotNull(endParameterName);
		this.startParameterName = startParameterName;
		this.endParameterName = endParameterName;
		startfield = new DateField(caption, new DateTime().withTimeAtStartOfDay().toDate());
		startfield.setResolution(Resolution.DAY);
		startfield.setDateFormat("yyyy/MM/dd");

		startfield.setValidationVisible(true);

		endfield = new DateField("To", new DateTime().withTimeAtStartOfDay().toDate());
		endfield.setResolution(Resolution.DAY);
		endfield.setDateFormat("yyyy/MM/dd");

		endfield.setValidationVisible(true);
		createValidators();

	}

	void createValidators()
	{
		startfield.addValidator(new Validator()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object value) throws InvalidValueException
			{
				if (value == null)
				{
					throw new InvalidValueException("Start date is invalid");
				}

				if (endfield.getValue() != null && ((Date) value).after(endfield.getValue()))
				{
					throw new InvalidValueException("Start date must be before the end date");
				}

			}
		});
		endfield.addValidator(new Validator()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object value) throws InvalidValueException
			{
				if (value == null)
				{
					throw new InvalidValueException("End date is invalid");
				}

				if (startfield.getValue() != null && ((Date) value).before(startfield.getValue()))
				{
					throw new InvalidValueException("Start date must be before the end date");
				}

			}
		});
	}

	@Override
	public String getValue(String parameterName)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(parameterFormat);

		return sdf.format(getDate(parameterName));
	}

	public Date getDate(String parameterName)
	{
		Date value;
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			value = startfield.getValue();
		}
		else if (parameterName.equalsIgnoreCase(endParameterName))
		{
			value = new DateTime(endfield.getValue()).plusDays(1).toDate();
		}
		else
		{
			throw new RuntimeException("Attempt to retrieve invalid parameter name " + parameterName
					+ " valid names are " + startParameterName + "," + endParameterName);
		}
		return value;
	}

	@Override
	public Component getComponent()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(startfield);
		layout.addComponent(endfield);
		return layout;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
	}

	@Override
	public void setDefaultValue(String defaultValue)
	{
		// this.field.setValue(defaultValue);

	}

	@Override
	public String getExpectedParameterClassName()
	{
		return null;
	}

	@Override
	public String getDisplayValue(String parameterName)
	{

		return new SimpleDateFormat(startfield.getDateFormat()).format(getDate(parameterName));
	}

	@Override
	public boolean validate()
	{
		boolean valid = false;
		try
		{
			startfield.validate();
			endfield.validate();
			valid = true;
		}
		catch (Exception e)
		{

		}
		return valid;
	}

	@Override
	public void setValueAsString(String value, String parameterName) throws ReadOnlyException, ConversionException,
			ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(parameterFormat);
		DateField field;
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			field = startfield;
		}
		else
		{
			field = endfield;
		}
		field.setValue(sdf.parse(value));

	}

	@Override
	public boolean isDateField()
	{
		return true;
	}

	@Override
	public DateParameterType getDateParameterType()
	{
		return DateParameterType.DATE;
	}

	public Date getStartDate()
	{
		return getDate(startParameterName);
	}

	public Date getEndDate()
	{
		return getDate(endParameterName);
	}

	public void setStartDate(Date date)
	{
		startfield.setValue(date);

	}

	public void setEndDate(Date date)
	{
		endfield.setValue(date);

	}

}
