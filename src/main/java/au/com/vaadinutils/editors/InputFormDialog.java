package au.com.vaadinutils.editors;

import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
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

	public InputFormDialog(final UI parent, String title, Field<?> primaryFocusField, final FormLayout form,
			final InputFormDialogRecipient recipient)
	{
		setCaption(title);
		setModal(true);
		this.setClosable(false);
		this.setResizable(false);

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeUndefined();
		layout.addComponent(form);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		buttons.addComponent(new Button("Cancel", new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				recipient.onCancel();
				close();
			}
		}));

		Button ok = new Button("Ok", new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				try
				{
					Iterator<Component> itr = form.iterator();
					while (itr.hasNext())
					{
						Field<?> field = (Field<?>) itr.next();
						field.validate();
					}
					recipient.onOK();
					close();
				}
				catch (InvalidValueException e)
				{
					Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
				}
			}
		});

		ok.setClickShortcut(KeyCode.ENTER);
		ok.addStyleName("default");
		buttons.addComponent(ok);

		layout.addComponent(buttons);
		layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);

		this.setContent(layout);
		parent.addWindow(this);

		primaryFocusField.focus();
	}
}