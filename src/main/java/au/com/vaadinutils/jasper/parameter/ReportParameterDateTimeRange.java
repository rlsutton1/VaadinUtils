package au.com.vaadinutils.jasper.parameter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.jasper.scheduler.entities.DateParameterOffsetType;
import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

public class ReportParameterDateTimeRange extends ReportParameter<String>
{
	protected DateField startfield;
	protected DateField endfield;

	protected String parameterFormat = "yyyy/MM/dd HH:mm:ss";
	protected String endParameterName;
	protected final String startParameterName;

	int endAdjustment = 0;
	protected ComboBox offsetType;

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
			Resolution resolution, String displayFormat, String parameterFormat, int endAdjustment)
	{
		super(caption, new String[] { startParameterName, endParameterName });
		Preconditions.checkNotNull(startParameterName);
		Preconditions.checkNotNull(endParameterName);
		this.startParameterName = startParameterName;
		this.endParameterName = endParameterName;
		startfield = new DateField(caption, new DateTime().withTimeAtStartOfDay().toDate());
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
		this.endAdjustment = endAdjustment;
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

		endAdjustment = -1;

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

		DateParameterOffsetType type = (DateParameterOffsetType) offsetType.getValue();
		Date value;
		if (parameterName.equalsIgnoreCase(startParameterName))
		{
			value = type.convertStartDate(startfield.getValue(), new Date(), getDateParameterType());
		}
		else if (parameterName.equalsIgnoreCase(endParameterName))
		{
			value = new DateTime(endfield.getValue()).plusDays(endAdjustment).toDate();
			value = type.convertEndDate(value, new Date(), getDateParameterType());
		}
		else
		{
			throw new RuntimeException("Attempt to retrieve invalid date parameter name " + parameterName
					+ " valid names are " + startParameterName + "," + endParameterName);
		}
		return value;
	}

	@Override
	public Component getComponent()
	{
		VerticalLayout layout = new VerticalLayout();

		List<DateParameterOffsetType> types = new LinkedList<>();
		for (DateParameterOffsetType type : DateParameterOffsetType.values())
		{
			types.add(type);
		}

		offsetType = new ComboBox("Date Options", types);
		offsetType.setImmediate(true);
		offsetType.setNullSelectionAllowed(false);
		offsetType.setWidth("140");
		offsetType.setValue(DateParameterOffsetType.CONSTANT);

		offsetType.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 7081417825842355432L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				DateParameterOffsetType offsetTypeValue = (DateParameterOffsetType) event.getProperty().getValue();
				startfield.setVisible(offsetTypeValue == DateParameterOffsetType.CONSTANT);
				endfield.setVisible(offsetTypeValue == DateParameterOffsetType.CONSTANT);

			}
		});
		layout.addComponent(offsetType);

		VerticalLayout inset = new VerticalLayout();
		inset.setMargin(new MarginInfo(false, false, false, true));

		inset.addComponent(startfield);
		inset.addComponent(endfield);
		layout.addComponent(inset);
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

		Date date = getDate(parameterName);
		if ("ReportParameterEndDate".equalsIgnoreCase(parameterName))
		{
			// actual end date will be 25/10/2017 00:00:00.0 but we want to
			// display 24/10/2017

			date = new DateTime(date).minusDays(endAdjustment).toDate();

		}

		return new SimpleDateFormat(startfield.getDateFormat()).format(date);
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
	public void setValueAsString(String value, String parameterName)
			throws ReadOnlyException, ConversionException, ParseException
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

	@Override
	public Date getStartDate()
	{
		return getDate(startParameterName);
	}

	@Override
	public Date getEndDate()
	{
		return getDate(endParameterName);
	}

	@Override
	public void setStartDate(Date date)
	{
		startfield.setValue(date);

	}

	@Override
	public void setEndDate(Date date)
	{
		endfield.setValue(date);

	}

	@Override
	public String getLabel(String parameterName)
	{
		if (parameterName.equalsIgnoreCase(endParameterName))
		{
			return "To";
		}

		return "From";
	}

	@Override
	public String getSaveMetaData()
	{
		return ((DateParameterOffsetType) offsetType.getValue()).name();
	}

	@Override
	public void applySaveMetaData(String metaData)
	{
		offsetType.setValue(DateParameterOffsetType.valueOf(metaData));
	}

	@Override
	public String getMetaDataComment()
	{
		return ((DateParameterOffsetType) offsetType.getValue()).toString();
	}
}
