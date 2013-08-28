package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;

public class SplitDateField extends DateField implements SplitField
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8992992230293865429L;
	private Label label;

	public SplitDateField(String label)
	{
		this.label = new Label(label);

	}

	@Override
	public void setVisible(boolean visible)
	{
		label.setVisible(visible);
		super.setVisible(visible);
	}

	@Override
	public Label getLabel()
	{
		return label;
	}

	@Override
	public String getCaption()
	{
		return label.getValue();
	}
}
