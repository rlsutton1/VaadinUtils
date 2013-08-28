package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;

public class SplitLabel extends Label implements SplitField
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5824753575628319814L;

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
