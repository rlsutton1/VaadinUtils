package au.com.vaadinutils.editors;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class InputFormDialog extends Window
{

	Logger logger = LogManager.getLogger();
	private HorizontalLayout buttons;
	private Button cancelButton;
	private Button ok;

	public InputFormDialog(final UI parent, String title, Field<?> primaryFocusField, final AbstractLayout form,
			final InputFormDialogRecipient recipient)
	{
		setCaption(title);

		setModal(true);
		this.setClosable(false);
		this.setResizable(false);

		VerticalLayout layout = new VerticalLayout();
		// layout.setMargin(new MarginInfo(true, true, true, false));
		layout.setMargin(true);
		layout.setSpacing(true);
		// layout.setSizeFull();
		layout.addComponent(form);
		// layout.setComponentAlignment(form, Alignment.TOP_CENTER);

		buttons = new HorizontalLayout();
		// buttons.setSpacing(true);
		// buttons.setHeight("50");

		cancelButton = new Button("Cancel", new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				if (recipient.onCancel())
				{
					close();
				}
			}
		});
		buttons.addComponent(cancelButton);

		ok = new Button("Ok", new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				try
				{
					Iterator<Component> itr = form.iterator();
					while (itr.hasNext())
					{
						Component comp = itr.next();
						if (comp instanceof Field<?>)
						{
							Field<?> field = (Field<?>) comp;
							field.validate();
						}
					}
					if (recipient.onOK())
					{
						close();
					}
				}
				catch (InvalidValueException e)
				{
					Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
				}
				catch (Exception e)
				{
					Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
					logger.error(e, e);
				}
			}
		});

		ok.setId("Ok");

		ok.setClickShortcut(KeyCode.ENTER);
		ok.addStyleName("default");
		buttons.addComponent(ok);

		layout.addComponent(buttons);
		layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
		layout.setExpandRatio(form, 1);

		this.setContent(layout);
		parent.addWindow(this);

		primaryFocusField.focus();

		if (form instanceof FormLayout)
		{
			setWidth("500");
		}
		// setHeight("150");

	}

	public void okOnly()
	{
		buttons.removeComponent(cancelButton);
	}

	public void setButtonsSpacing(boolean spacing)
	{
		buttons.setSpacing(spacing);
	}

	public void setOkButtonLabel(String label)
	{
		ok.setCaption(label);
	}

	public void setCancelButtonLabel(String label)
	{
		cancelButton.setCaption(label);
	}

	public void showOkButton(boolean show)
	{
		ok.setVisible(show);
	}

}
