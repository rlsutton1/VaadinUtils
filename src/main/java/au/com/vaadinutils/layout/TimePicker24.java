package au.com.vaadinutils.layout;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("rawtypes")
public class TimePicker24 extends HorizontalLayout implements Field
{
	Logger logger = LogManager.getLogger();

	protected static final String TIME_FORMAT = "HH:mm a";
	protected static final String TIME_FORMAT2 = "HH:mma";

	private static final String PM = "pm";
	private static final String AM = "am";
	private static final long serialVersionUID = 1826417125815798837L;
	private static final String EMPTY = "--:--";
	String headerColor = "#B2D7FF";
	final private TextField displayTime = new TextField();
	String hour = "12";
	String minute = "00";
	boolean isSet = false;
	// private Button zeroHourButton;
	// private Button zeroMinuteButton;
	private String title;
	private TextField field;
	private Property<Date> datasource;
	// private boolean isRequired;
	private String requiredErrorMessage;
	private int tabIndex;
	private boolean isBuffered;
	private Validator validator;
	private SimpleDateFormat sdf;
	private String amPm;
	private Button pickerButton;

	public TimePicker24(String title, String format)
	{
		sdf = new SimpleDateFormat(format);
		DateFormatSymbols symbols = new DateFormatSymbols();
		symbols.setAmPmStrings(new String[] { AM, PM });
		sdf.setDateFormatSymbols(symbols);

		setCaption(title);
		field = new TextField();
		field.setWidth("125");
		field.setImmediate(true);
		displayTime.setImmediate(true);
		validator = new Validator()
		{

			private static final long serialVersionUID = 6579163030027373837L;

			@Override
			public void validate(Object value) throws InvalidValueException
			{
				if (value == null || value.equals(EMPTY))
				{
					return;
				}
				try
				{
					sdf.parse((String) value);
				}
				catch (ParseException e)
				{
					throw new InvalidValueException("Invalid time" + value);
				}

			}
		};
		displayTime.addValidator(validator);
		field.addValidator(validator);

		field.addValueChangeListener(new ValueChangeListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{

				if (event.getProperty().getValue() != null)
				{
					try
					{
						final Date parsedDate = parseDate((String) event.getProperty().getValue());
						if (parsedDate != null)
						{
							setValue(parsedDate);
						}
					}
					catch (InvalidValueException e)
					{
						Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
					}
				}
			}
		});

		this.title = title;

		CssLayout hl = new CssLayout();
		hl.addStyleName("v-component-group");
		hl.setWidth("100%");
		hl.setHeight("35");

		pickerButton = new Button();
		pickerButton.setIcon(FontAwesome.CLOCK_O);

		hl.addComponent(field);
		hl.addComponent(pickerButton);
		pickerButton.addClickListener(new ClickListener()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				showPopupTimePicker();

			}
		});
		addComponent(hl);
	}

	protected Date parseDate(String value)
	{
		if (value == null || value.equals(EMPTY))
		{
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
		try
		{
			return sdf.parse(value);
		}
		catch (ParseException e)
		{
			sdf = new SimpleDateFormat(TIME_FORMAT2);
			try
			{
				return sdf.parse(value);
			}
			catch (ParseException e2)
			{
				throw new InvalidValueException("Time format is " + TIME_FORMAT);
			}
		}
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		pickerButton.setReadOnly(true);
	}

	public void focus()
	{
		super.focus();
	}

	private void showPopupTimePicker()
	{

		String value = field.getValue();
		try
		{

			String[] parts = value.split(":");
			hour = parts[0];
			String[] np = parts[1].split(" ");
			minute = np[0];
			displayTime.setValue(value);
		}
		catch (Exception e)
		{
			clearValue();
		}

		final Window window = new Window(title);
		window.setModal(true);
		window.setResizable(false);
		window.setWidth("430");
		window.setHeight("240");
		window.setClosable(false);

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
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

		HorizontalLayout hourButtonPanel = new HorizontalLayout();
		hourPanel.addComponent(hourButtonPanel);
		addHourButtons(hourButtonPanel, 3, 8);

		HorizontalLayout minutePanel = new HorizontalLayout();
		// minutePanel.setPadding(5);
		HorizontalLayout minuteButtonPanel = new HorizontalLayout();
		minutePanel.addComponent(minuteButtonPanel);
		addMinuteButtons(minuteButtonPanel, 2, 4);

		HorizontalLayout amPmHourWrapper = new HorizontalLayout();
		amPmHourWrapper.addComponent(hourPanel);
		hourPanelLabelWrapper.addComponent(amPmHourWrapper);

		layout.addComponent(hourPanelLabelWrapper);

		minuteLabelWrapper.addComponent(minutePanel);
		layout.addComponent(minuteLabelWrapper);
		layout.setExpandRatio(hourPanelLabelWrapper, 0.7f);
		layout.setExpandRatio(minuteLabelWrapper, 0.3f);

		HorizontalLayout okcancel = new HorizontalLayout();
		okcancel.setSizeFull();
		Button ok = new Button("OK");

		ok.setWidth("75");
		ok.addClickListener(new ClickListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{

				try
				{
					displayTime.validate();
					if (displayTime.getValue().equals(EMPTY))
					{
						field.setValue("");
					}
					else
					{
						field.setValue(displayTime.getValue());
					}
					window.close();
				}
				catch (InvalidValueException e)
				{

				}

			}
		});

		Button cancel = new Button("Cancel");
		cancel.setWidth("75");
		cancel.addClickListener(new ClickListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				window.close();

			}
		});
		okcancel.addComponent(displayTime);

		okcancel.addComponent(cancel);
		okcancel.addComponent(ok);
		okcancel.setMargin(true);
		okcancel.setSpacing(true);
		okcancel.setExpandRatio(displayTime, 0.50f);
		// okcancel.setExpandRatio(ok, 0.25f);
		// okcancel.setExpandRatio(cancel, 0.25f);

		VerticalLayout wrapper = new VerticalLayout();
		// wrapper.setStyleName("njadmin-search-colour");

		wrapper.setSizeFull();
		wrapper.addComponent(layout);
		wrapper.addComponent(okcancel);
		wrapper.setExpandRatio(layout, 0.9f);
		wrapper.setExpandRatio(okcancel, 0.5f);
		window.setContent(wrapper);

		UI.getCurrent().addWindow(window);

	}

	private void addMinuteButtons(HorizontalLayout minuteButtonPanel, int rows, int cols)
	{
		String[] numbers = new String[] { "00", "10", "15", "20", "30", "40", "45", "50" };
		for (int col = 0; col < cols; col++)
		{
			VerticalLayout rowsLayout = new VerticalLayout();
			for (int row = 0; row < rows; row++)
			{

				final NativeButton button = new NativeButton("" + numbers[row + (col * rows)]);
				rowsLayout.addComponent(button);

				button.setStyleName(Reindeer.BUTTON_SMALL);

				button.setWidth("30");
				// button.setHeight("30");
				// button.setAutoFit(false);
				// button.setActionType(SelectionType.RADIO);
				// button.addToRadioGroup("minuteButtons");
				// if (row == 0 && col == 0)
				// {
				// zeroMinuteButton = button;
				//
				// }

				button.addClickListener(new ClickListener()
				{

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event)
					{

						String title = button.getCaption();
						minute = title;
						isSet = true;
						setNewValue();

					}
				});

			}
			minuteButtonPanel.addComponent(rowsLayout);
		}
	}

	private void addHourButtons(HorizontalLayout hourButtonPanel, int rows, int cols)
	{
		int[] numbers = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
				23 };
		for (int col = 0; col < cols; col++)
		{
			VerticalLayout rowsLayout = new VerticalLayout();
			for (int row = 0; row < rows; row++)
			{
				final NativeButton button = new NativeButton("" + numbers[col + (row * cols)]);
				rowsLayout.addComponent(button);
				button.setStyleName(Reindeer.BUTTON_SMALL);
				button.setWidth("30");
				// button.setHeight("15");
				// button.setAutoFit(false);

				// button.setActionType(SelectionType.RADIO);
				// button.addToRadioGroup("hourButtons");
				// if (row == 0 && col == 0)
				// {
				// zeroHourButton = button;
				//
				// }

				button.addClickListener(new ClickListener()
				{

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event)
					{
						String title = button.getCaption();
						hour = title;
						isSet = true;
						amPm = AM;
						if (Integer.parseInt(hour) > 11)
						{
							amPm = PM;
						}
						setNewValue();

					}
				});

			}
			hourButtonPanel.addComponent(rowsLayout);
		}
	}

	// public void addChangedHandler(ChangedHandler pChangedHandler)
	// {
	// this.changedHandler = pChangedHandler;
	//
	// }

	public void clearValue()
	{

		// am.setSelected(true);
		// am.setSelected(false);
		// zeroHourButton.setSelected(true);
		// zeroHourButton.setSelected(false);
		// zeroMinuteButton.setSelected(true);
		// zeroMinuteButton.setSelected(false);
		hour = "12";
		minute = "00";
		isSet = false;
		displayTime.setValue(EMPTY);
		field.setValue(EMPTY);

	}

	public final String getValueAsString()
	{
		if (isSet)
		{
			String tmp = "" + hour + ":" + minute + amPm;
			Date date;
			try
			{
				date = sdf.parse(tmp);
				return sdf.format(date);
			}
			catch (ParseException e)
			{

			}
			return tmp;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public void setValues(Date date)
	{
		hour = "" + date.getHours();
		minute = "" + date.getMinutes();
		if (date.getMinutes() < 10)
		{
			minute = "0" + minute;
		}
		amPm = AM;
		if (Integer.parseInt(hour) > 11)
		{
			amPm = PM;
		}
		isSet = true;
		displayTime.setValue(getValueAsString());
		field.setValue(getValueAsString());
		// logger.info("set to " + getValueAsString());
	}

	private void setNewValue()
	{
		displayTime.setValue(getValueAsString());
	}

	@Override
	public boolean isInvalidCommitted()
	{
		return false;
	}

	@Override
	public void setInvalidCommitted(boolean isCommitted)
	{
	}

	@Override
	public void commit() throws SourceException, InvalidValueException
	{
		datasource.setValue((Date) getValue());

	}

	@Override
	public void discard() throws SourceException
	{
		setValues(datasource.getValue());

	}

	@Override
	public void setBuffered(boolean buffered)
	{
		this.isBuffered = buffered;

	}

	@Override
	public boolean isBuffered()
	{
		return isBuffered;
	}

	@Override
	public boolean isModified()
	{
		Date value = (Date) getValue();
		if (datasource == null)
		{
			return false;
		}
		Date dsValue = datasource.getValue();
		if (dsValue == null)
		{
			return value != null;
		}
		if (value == null)
		{
			return true;
		}

		return !dsValue.equals(value);
	}

	@Override
	public void addValidator(Validator validator)
	{
	}

	@Override
	public void removeValidator(Validator validator)
	{
	}

	@Override
	public void removeAllValidators()
	{
	}

	@Override
	public Collection<Validator> getValidators()
	{
		Collection<Validator> validators = new LinkedList<Validator>();
		validators.add(validator);
		return validators;
	}

	@Override
	public boolean isValid()
	{
		boolean valid = true;
		try
		{
			validator.validate(getValueAsString());
		}
		catch (Exception e)
		{
			valid = false;
		}
		return valid;
	}

	@Override
	public void validate() throws InvalidValueException
	{
		validator.validate(getValueAsString());

	}

	@Override
	public boolean isInvalidAllowed()
	{
		return false;
	}

	@Override
	public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException
	{
	}

	@Override
	public Object getValue()
	{
		try
		{
			if (getValueAsString() == null)
			{
				return null;
			}
			return sdf.parse(getValueAsString());
		}
		catch (ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValue(Object newValue)
	{
		setValues((Date) newValue);

	}

	@Override
	public Class getType()
	{

		return Date.class;
	}

	@Override
	public void addValueChangeListener(ValueChangeListener listener)
	{
	}

	@Override
	public void addListener(ValueChangeListener listener)
	{
	}

	@Override
	public void removeValueChangeListener(ValueChangeListener listener)
	{
	}

	@Override
	public void removeListener(ValueChangeListener listener)
	{
	}

	@Override
	public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
	{
		logger.info("Value change");

	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPropertyDataSource(Property newDataSource)
	{
		clearValue();
		datasource = newDataSource;
		if (datasource.getValue() != null)
		{
			setValues(datasource.getValue());
		}

	}

	@Override
	public Property<Date> getPropertyDataSource()
	{
		return datasource;
	}

	@Override
	public int getTabIndex()
	{
		return tabIndex;
	}

	@Override
	public void setTabIndex(int tabIndex)
	{
		this.tabIndex = tabIndex;

	}

	@Override
	public boolean isRequired()
	{
		return isVisible();

	}

	@Override
	public void setRequired(boolean required)
	{
		// isRequired = required;

	}

	@Override
	public void setRequiredError(String requiredMessage)
	{
		requiredErrorMessage = requiredMessage;

	}

	@Override
	public String getRequiredError()
	{
		return requiredErrorMessage;
	}

	@Override
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub

	}

}
