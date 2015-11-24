package au.com.vaadinutils.layout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class DateTimePickerInline extends TimePicker
{
	private static final long serialVersionUID = 1L;

	InlineDateField datePicker;

	SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE yyyy/MM/dd");

	public DateTimePickerInline(String title)
	{
		super(title);
	}

	protected void buildUI(String title)
	{

		datePicker = new InlineDateField(title + " Date");
		datePicker.setDateFormat("yyyy/MM/dd");
		datePicker.setValue(new Date());

		final Label displayDate = new Label();

		datePicker.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(Property.ValueChangeEvent event)
			{
				final Date date = (Date) event.getProperty().getValue();

				displayDate.setValue(dateFormatter.format(date));

			}
		});

		final Label midnightLabel = new Label();
		midnightLabel.setContentMode(ContentMode.HTML);

		field = new TextField();
		field.setWidth("125");
		field.setImmediate(true);
		displayTime = field;
		displayTime.addValidator(timeValidator);
		field.addValidator(timeValidator);
		field.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{
				DateTimePickerInline.this.valueChange(event);
				@SuppressWarnings("deprecation")
				int hour = getValue().getHours();
				if (hour == 0)
				{
					midnightLabel.setValue("<font color='red'><b>Midnight</b></font>");
				}
				else
				{
					midnightLabel.setValue("");
				}
				try
				{
					final Date parsedDate = parseDate((String) event.getProperty().getValue());
					if (parsedDate != null)
					{
						dateTime.set(Calendar.HOUR_OF_DAY, parsedDate.getHours());
						dateTime.set(Calendar.MINUTE, parsedDate.getMinutes());
						isSet = true;
						setNewValue();
					}
				}
				catch (InvalidValueException e)
				{
					logger.info(e);
					// the validator will handle this
				}

			}
		});

		field.setCaption(null);
		datePicker.setCaption(null);

		VerticalLayout vl = new VerticalLayout();

		vl.addComponent(datePicker);

		vl.addComponent(displayDate);

		HorizontalLayout timeFieldLayout = new HorizontalLayout();
		timeFieldLayout.setSpacing(true);
		timeFieldLayout.addComponent(field);
		timeFieldLayout.addComponent(midnightLabel);
		timeFieldLayout.setComponentAlignment(midnightLabel, Alignment.MIDDLE_LEFT);
		vl.addComponent(timeFieldLayout);

		vl.addComponent(buildInline());
		addComponent(vl);
	}

	@Override
	public void validate() throws InvalidValueException
	{
		super.validate();
		displayTime.validate();
		field.validate();

	}

	@SuppressWarnings("deprecation")
	private VerticalLayout buildInline()
	{

		try
		{
			Date value = parseDate(field.getValue());
			int hourNumber = value.getHours() % 12;
			if (hourNumber == 0)
			{
				hourNumber = 12;
			}

			displayTime.setValue(field.getValue());
		}
		catch (Exception e)
		{
			logger.warn(e);
			clearValue();
		}

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		// layout.setMargin(true);
		layout.setSpacing(true);
		layout.setStyleName(Reindeer.BUTTON_SMALL);

		displayTime.setWidth("100");

		VerticalLayout hourPanelLabelWrapper = new VerticalLayout();

		Label hourLabel = new Label("Hour");

		// hourLabel.setBackgroundColor(headerColor);
		hourLabel.setWidth("230");
		// hourLabel.setHeight("30");
		// hourLabel.setAutoFit(false);
		HorizontalLayout innerHourLabelPanel = new HorizontalLayout();

		// innerHourLabelPanel.setPadding(5);
		innerHourLabelPanel.addComponent(hourLabel);
		innerHourLabelPanel.setWidth("100");
		// innerHourLabelPanel.setHeight("30");
		hourPanelLabelWrapper.addComponent(innerHourLabelPanel);

		VerticalLayout minuteLabelWrapper = new VerticalLayout();
		minuteLabelWrapper.setWidth("60");
		Label minuteLabel = new Label("Minute");
		// minuteLabel.setBackgroundColor(headerColor);
		// minuteLabel.setStyleName("njadmin-search-colour");

		minuteLabel.setWidth("45");
		// minuteLabel.setHeight("30");
		// minuteLabel.setAutoFit(false);
		VerticalLayout innerMinuteLabelPanel = new VerticalLayout();
		// innerMinuteLabelPanel.setPadding(5);
		innerMinuteLabelPanel.addComponent(minuteLabel);
		innerMinuteLabelPanel.setWidth("45");
		// innerMinuteLabelPanel.setHeight("30");

		minuteLabelWrapper.addComponent(innerMinuteLabelPanel);

		HorizontalLayout hourPanel = new HorizontalLayout();
		// hourPanel.setPadding(5);

		HorizontalLayout amPmPanel = new HorizontalLayout();
		// amPmPanel.setPadding(5);

		VerticalLayout amPmButtonPanel = new VerticalLayout();
		amPmPanel.addComponent(amPmButtonPanel);
		addAmPmButtons(amPmButtonPanel);

		HorizontalLayout hourButtonPanel = new HorizontalLayout();
		hourPanel.addComponent(hourButtonPanel);
		addHourButtons(hourButtonPanel, 2, 6);

		HorizontalLayout minutePanel = new HorizontalLayout();
		// minutePanel.setPadding(5);
		HorizontalLayout minuteButtonPanel = new HorizontalLayout();
		minutePanel.addComponent(minuteButtonPanel);
		addMinuteButtons(minuteButtonPanel, 2, 4);

		HorizontalLayout amPmHourWrapper = new HorizontalLayout();
		amPmHourWrapper.addComponent(hourPanel);
		amPmHourWrapper.addComponent(amPmPanel);
		hourPanelLabelWrapper.addComponent(amPmHourWrapper);

		layout.addComponent(hourPanelLabelWrapper);

		minuteLabelWrapper.addComponent(minutePanel);
		layout.addComponent(minuteLabelWrapper);
		layout.setExpandRatio(hourPanelLabelWrapper, 0.7f);
		layout.setExpandRatio(minuteLabelWrapper, 0.3f);

		return layout;

	}

	@Override
	public Date getValue()
	{
		Date value = null;
		if (isSet)
		{
			value = dateTime.getTime();
		}
		DateTime date = new DateTime(datePicker.getValue());
		return new DateTime(value).withYear(date.getYear()).withMonthOfYear(date.getMonthOfYear())
				.withDayOfMonth(date.getDayOfMonth()).toDate();

	}

	public void setValues(Date date)
	{
		if (date != null)
		{
			dateTime.setTime(date);
			datePicker.setValue(date);
			isSet = true;
			displayTime.setValue(getValueAsString());
			internalSetReadonlyFieldValue(getValueAsString());
		}
		else
		{
			clearValue();
		}
	}

	protected void setNewValue()
	{
		displayTime.setValue(getValueAsString());
		internalSetReadonlyFieldValue(getValueAsString());
		if (changedHandler != null)
		{
			changedHandler.onChanged(getValueAsString());

		}
	}

}
