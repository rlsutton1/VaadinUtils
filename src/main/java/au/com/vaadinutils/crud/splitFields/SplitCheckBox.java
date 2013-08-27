package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;

public class SplitCheckBox extends CheckBox implements SplitField
{
	private Label label;

	public SplitCheckBox(String label)
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

}
