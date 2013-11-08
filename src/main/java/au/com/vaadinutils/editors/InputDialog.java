package au.com.vaadinutils.editors;

import com.vaadin.data.Validator;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class InputDialog extends Window implements InputFormDialogRecipient
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8135921877987898679L;
	final Recipient recipient;
	final TextField field = new TextField();

	public InputDialog(final UI parent, String title, String question, Recipient recipient)
	{
		this.recipient = recipient;
		
		FormLayout form = new FormLayout();

		field.setCaption(question);
		form.addComponent(field);
		new InputFormDialog(parent, title, field,  form, this);
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


	@Override
	public boolean onOK()
	{
		return recipient.onOK(field.getValue());
		
	}


	@Override
	public boolean onCancel()
	{
		return recipient.onCancel();
		
	}
}
