package au.com.vaadinutils.fields;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * A Vaadin label that allows actions to be run by adding a LayoutClickListener
 */
@SuppressWarnings("serial")
public class ClickableLabel extends VerticalLayout
{
	private Label label;

	public ClickableLabel()
	{
		this(null);
		setImmediate(true);
	}

	public ClickableLabel(String value)
	{
		label = new Label(value, ContentMode.HTML);
		addComponent(label);
	}

	public void setValue(String value)
	{
		label.setValue(value);
	}

	public void setStyleName(String style)
	{
		label.setStyleName(style);
	}

	public void setContentMode(ContentMode contentMode)
	{
		label.setContentMode(contentMode);

	}

	public String getValue()
	{
		return label.getValue() != null ? label.getValue() : "";
	}
}
