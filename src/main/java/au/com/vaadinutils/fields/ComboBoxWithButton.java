package au.com.vaadinutils.fields;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;

@SuppressWarnings("serial")
public class ComboBoxWithButton extends FieldWithButton<Object>
{
	public ComboBoxWithButton(final String caption)
	{
		super(caption, null);
	}

	public ComboBoxWithButton(final String caption, final Button button)
	{
		super(caption, button);
	}

	@Override
	protected AbstractField<Object> createField()
	{
		return new ComboBox();
	}

	public void setNullSelectionAllowed(final boolean nullSelectionAllowed)
	{
		((ComboBox) field).setNullSelectionAllowed(nullSelectionAllowed);
	}

	public void setContainerDataSource(final Container newDataSource)
	{
		((ComboBox) field).setContainerDataSource(newDataSource);
	}
}
