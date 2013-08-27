package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;

public class SplitLabel extends Label implements SplitField
{
	private Label label;

	public SplitLabel(String label)
	{
		super(label);

	}

	@Override
	public Label getLabel()
	{
		return this;
	}

}
