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
    private InputFormDialog dialog;

    public InputDialog(final UI parent, String title, String question, Recipient recipient)
    {
	this.recipient = recipient;

	FormLayout form = new FormLayout();
	form.setMargin(true);

	field.setCaption(question);
	form.addComponent(field);
	dialog = new InputFormDialog(parent, title, field, form, this);
	dialog.setWidth("500");
	dialog.setHeight("150");

    }

    public void setWidth(String width)
    {
	dialog.setWidth(width);
    }

    public void setHeight(String width)
    {
	dialog.setHeight(width);
    }

    public void setFieldWidth(String width)
    {
	field.setWidth(width);
    }

    public void setFieldHeight(String width)
    {
	field.setHeight(width);
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
