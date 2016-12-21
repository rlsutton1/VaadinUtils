package au.com.vaadinutils.editors;

import com.vaadin.data.Validator;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

public class InputDialog implements InputFormDialogRecipient
{
	final Recipient recipient;
	final TextField field = new TextField();
	private InputFormDialog dialog;

	public InputDialog(final UI parent, String title, String question, Recipient recipient)
	{
		this.recipient = recipient;

		FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setSizeFull();

		field.setCaption(question);
		field.setSizeFull();
		form.addComponent(field);
		dialog = new InputFormDialog(parent, title, field, form, this);

	}

	public void setOkButtonLabel(String label)
	{
		dialog.setOkButtonLabel(label);
	}

	public void setCancelButtonLabel(String label)
	{
		dialog.setCancelButtonLabel(label);

	}

	public void setFieldWidth(String width)
	{
		field.setWidth(width);
	}

	public void setFieldHeight(String width)
	{
		field.setHeight(width);
	}

	public void setDefaultValue(String value)
	{
		field.setValue(value);
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
