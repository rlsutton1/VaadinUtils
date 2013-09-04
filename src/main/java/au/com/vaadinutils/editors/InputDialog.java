package au.com.vaadinutils.editors;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class InputDialog extends Window
{
	final Recipient recipient;
	final TextField field = new TextField();

	public InputDialog(final UI parent, String title, String question, Recipient recipient)
	{
		this.recipient = recipient;
		setCaption(title);
		setModal(true);
		this.setClosable(false);
		this.setResizable(false);

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeUndefined();
		FormLayout form = new FormLayout();

		field.setCaption(question);
		form.addComponent(field);
		layout.addComponent(form);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		buttons.addComponent(new Button("Cancel", new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				InputDialog.this.recipient.onCancel();
				InputDialog.this.close();
			}
		}));
		
		Button ok = new Button("Ok", new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				try
				{
					field.validate();
					InputDialog.this.recipient.onOK(field.getValue());
					InputDialog.this.close();
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
		
		field.focus();
	}

	public interface Recipient
	{
		public void onOK(String input);

		public void onCancel();
	}

	/**
	 * Add validators to the field which will be run when the user clicks OK.
	 * 
	 * The OK button will not succeed whilst there are field validation errors.
	 * 
	 * @param validator
	 */
	public void addValidator(Validator validator)
	{
		field.addValidator(validator);
	}
}