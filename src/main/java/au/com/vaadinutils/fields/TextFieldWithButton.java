package au.com.vaadinutils.fields;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class TextFieldWithButton extends FieldWithButton<String>
{
	public TextFieldWithButton(final String caption)
	{
		super(caption, null);
	}

	public TextFieldWithButton(final String caption, final Button button)
	{
		super(caption, button);
	}

	@Override
	protected AbstractField<String> createField()
	{
		return new TextField();
	}

	public void setNullRepresentation(String nullRepresentation)
	{
		((TextField) field).setNullRepresentation(nullRepresentation);
	}
}
