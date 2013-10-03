package au.com.vaadinutils.layout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class TimePicker extends HorizontalLayout
{

	private static final long serialVersionUID = 1826417125815798837L;
	private static final String EMPTY = "--:--";
	String headerColor = "#B2D7FF";
	final private TextField displayTime = new TextField();
	String hour = "12";
	String minute = "00";
	String amPm = "AM";
	boolean isSet = false;
	private Button am;
	// private Button zeroHourButton;
	// private Button zeroMinuteButton;
	private ChangedHandler changedHandler;
	private String title;
	private TextField field;

	public TimePicker(String title)
	{

		setCaption(title);
		displayTime.addValidator(new Validator()
		{

			private static final long serialVersionUID = 6579163030027373837L;

			@Override
			public void validate(Object value) throws InvalidValueException
			{
				if (value.equals(EMPTY))
				{
					return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm aa");
				try
				{
					sdf.parse((String) value);
				}
				catch (ParseException e)
				{
					throw new InvalidValueException("Time formate is HH:mm aa");
				}

			}
		});

		this.title = title;
		HorizontalLayout hl = new HorizontalLayout();
		hl.setStyleName("v-datefield v-datefield-popupcalendar v-datefield-day");

		field = new TextField();
		field.setWidth("125");
		field.setStyleName("v-datefield-textfield");

		NativeButton b = new NativeButton();
		b.setStyleName("v-datefield-button");

		hl.addComponent(field);
		hl.addComponent(b);
		b.addClickListener(new ClickListener()
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

	private void showPopupTimePicker()
	{

		String value = field.getValue();
		try
		{
			String[] parts = value.split(":");
			hour = parts[0];
			String[] np = parts[1].split(" ");
			minute = np[0];
			amPm = np[1];
			if (!amPm.equals("AM") && !amPm.equals("PM"))
			{
				throw new Exception();
			}
			displayTime.setValue(value);
		}
		catch (Exception e)
		{
			clearValue();
		}

		final Window window = new Window(title);
		window.setModal(true);
		window.setResizable(false);
		window.setWidth("380");
		window.setHeight("180");
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
		addMinuteButtons(minuteButtonPanel, 2, 3);

		HorizontalLayout amPmHourWrapper = new HorizontalLayout();
		amPmHourWrapper.addComponent(amPmPanel);
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
		String[] numbers = new String[] { "00", "10", "20", "30", "40", "50" };
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
		int[] numbers = new int[] { 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
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
						if (displayTime.getValue().compareToIgnoreCase(EMPTY) == 0)
						{
							if (Integer.parseInt(hour) == 12 || Integer.parseInt(hour) < 8)
							{
								amPm = "PM";
							}
						}
						isSet = true;
						setNewValue();

					}
				});

			}
			hourButtonPanel.addComponent(rowsLayout);
		}
	}

	private void addAmPmButtons(VerticalLayout amPmButtonPanel)
	{
		am = new NativeButton("AM");
		final NativeButton pm = new NativeButton("PM");
		amPmButtonPanel.addComponent(am);
		amPmButtonPanel.addComponent(pm);
		// am.setActionType(SelectionType.RADIO);
		// am.addToRadioGroup("amPmButtons");
		// pm.setActionType(SelectionType.RADIO);
		// pm.addToRadioGroup("amPmButtons");

		am.setStyleName(Reindeer.BUTTON_SMALL);
		am.setWidth("35");
		// am.setHeight("22");
		// am.setAutoFit(false);

		am.addClickListener(new ClickListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				String title = am.getCaption();
				amPm = title;
				isSet = true;
				setNewValue();

			}
		});
		pm.setStyleName(Reindeer.BUTTON_SMALL);
		pm.setWidth("35");
		// pm.setHeight("22");
		// pm.setAutoFit(false);

		pm.addClickListener(new ClickListener()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event)
			{
				String title = pm.getCaption();
				amPm = title;
				isSet = true;
				displayTime.setValue(getValueAsString());

			}
		});

	}

	public void addChangedHandler(ChangedHandler pChangedHandler)
	{
		this.changedHandler = pChangedHandler;

	}

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
		amPm = "AM";
		isSet = false;
		displayTime.setValue(EMPTY);

	}

	public final String getValueAsString()
	{
		if (isSet)
			return "" + hour + ":" + minute + " " + amPm;
		return null;
	}

	@SuppressWarnings("deprecation")
	public void setValues(Date date)
	{
		hour = "" + date.getHours();
		minute = "" + date.getMinutes();
		isSet = true;
		amPm = "AM";
		if (date.getHours() >= 12)
		{
			amPm = "PM";
			if (date.getHours() > 12)
			{
				hour = "" + (date.getHours() - 12);
			}

		}
		if (date.getHours() == 0)
		{
			hour = "12";
		}
		displayTime.setValue(getValueAsString());
	}

	private void setNewValue()
	{
		displayTime.setValue(getValueAsString());
		if (changedHandler != null)
		{
			changedHandler.onChanged(getValueAsString());

		}
	}
}
