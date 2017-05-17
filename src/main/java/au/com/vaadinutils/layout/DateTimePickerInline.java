package au.com.vaadinutils.layout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.MouseEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;

public class DateTimePickerInline extends TimePicker
{
	private static final long serialVersionUID = 1L;

	InlineDateField datePicker;

	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");

	private TextField displayDate;

	public DateTimePickerInline(String title)
	{
		super(title);
	}

	@Override
	protected void buildUI(String title)
	{

		datePicker = new InlineDateField(title + " Date");
		datePicker.setDateFormat("yyyy/MM/dd");
		datePicker.setValue(new Date());

		displayDate = createTextDateField();

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
						Calendar parsed = Calendar.getInstance();
						parsed.setTime(parsedDate);
						dateTime.set(Calendar.HOUR_OF_DAY, parsed.get(Calendar.HOUR_OF_DAY));
						dateTime.set(Calendar.MINUTE, parsed.get(Calendar.MINUTE));
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

		HorizontalLayout dateLayout = new HorizontalLayout();
		dateLayout.setWidth("210");

		Button changeYearButton = new Button("Year");
		changeYearButton.setDescription("Change the year");
		changeYearButton.setStyleName(ValoTheme.BUTTON_TINY);
		changeYearButton.addClickListener(getChangeYearClickListener());

		dateLayout.addComponent(displayDate);
		dateLayout.addComponent(changeYearButton);
		dateLayout.setComponentAlignment(changeYearButton, Alignment.MIDDLE_RIGHT);

		vl.addComponent(datePicker);

		HorizontalLayout timeFieldLayout = new HorizontalLayout();
		timeFieldLayout.setSpacing(true);
		timeFieldLayout.addComponent(field);
		timeFieldLayout.addComponent(midnightLabel);
		timeFieldLayout.setComponentAlignment(midnightLabel, Alignment.MIDDLE_LEFT);

		vl.addComponent(buildInline());
		addComponent(vl);
		vl.addComponent(timeFieldLayout);

		vl.addComponent(dateLayout);
		vl.setSpacing(true);

	}

	private TextField createTextDateField()
	{
		final TextField displayDate = new TextField();

		// add validator to text date field
		displayDate.addValidator(new Validator()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object value) throws InvalidValueException
			{
				try
				{
					Date date = dateFormatter.parse((String) value);
					datePicker.setValue(date);
				}
				catch (ParseException e)
				{
					throw new InvalidValueException(e.getMessage());
				}
			}
		});

		// add value change listener to text date field
		displayDate.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(Property.ValueChangeEvent event)
			{
				try
				{
					Date date = dateFormatter.parse((String) event.getProperty().getValue());
					datePicker.setValue(date);
				}
				catch (ParseException e)
				{
					// do nothing, well handle this in a validator
				}

			}
		});
		return displayDate;
	}

	private ClickListener getChangeYearClickListener()
	{
		return new ClickListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{

				final Window window = new Window();

				GridLayout yearGrid = new GridLayout(5, 20);

				int currentYear = DateTime.now().getYear();
				int endYear = DateTime.now().getYear() + 10;
				int startYear = (endYear - 135);

				for (int year = endYear; year > startYear; year--)

				{
					final int buttonYear = year;
					Button yearButton = new Button("" + year);
					yearButton.setStyleName(ValoTheme.BUTTON_TINY);
					if (year == currentYear)
					{
						yearButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
					}
					yearButton.addClickListener(new ClickListener()
					{

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(ClickEvent event)
						{
							setValue(new DateTime(getValue().getTime()).withYear(buttonYear).toDate());
							window.close();
						}
					});
					yearGrid.addComponent(yearButton);
				}
				Panel scrollPanel = new Panel();
				VerticalLayout container = new VerticalLayout();
				container.addComponent(new Label("Select a year"));
				container.setMargin(true);
				container.addComponent(scrollPanel);

				scrollPanel.setContent(yearGrid);
				scrollPanel.setHeight("200");

				window.setResizable(false);

				window.setContent(container);

				// position window over year button
				int x = event.getClientX();
				int y = event.getClientY();

				int wh = UI.getCurrent().getPage().getBrowserWindowHeight();
				y = Math.max(0, y);
				int heightOfDatePicker = 300;
				y = Math.min(y, wh - heightOfDatePicker);

				int ww = UI.getCurrent().getPage().getBrowserWindowWidth();
				x = Math.max(0, x - 50);
				int widthOfDatePicker = 290;
				x = Math.min(x, ww - widthOfDatePicker);

				// window.setModal(true);
				window.setPosition(x, y);
				window.setClosable(true);

				UI.getCurrent().addWindow(window);

				// add mouse click listener to close window when the user
				// clicks outside of the window

				final MouseEvents.ClickListener listener = new MouseEvents.ClickListener()
				{

					private static final long serialVersionUID = 1L;

					@Override
					public void click(MouseEvents.ClickEvent event)
					{
						window.close();
					}
				};
				UI.getCurrent().addClickListener(listener);

				// tidy up: remove mouse click listener
				window.addCloseListener(new CloseListener()
				{

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e)
					{
						UI.getCurrent().removeClickListener(listener);

					}
				});

			}
		};
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
			logger.info(e);
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
		minuteButtonPanel.setHeight("70");

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

	@Override
	public void setValues(Date date)
	{
		if (date != null)
		{
			dateTime.setTime(date);
			datePicker.setValue(date);
			isSet = true;
			displayTime.setValue(getValueAsString());
			displayDate.setValue(dateFormatter.format(date));
			internalSetReadonlyFieldValue(getValueAsString());
		}
		else
		{
			clearValue();
		}
	}

	@Override
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
