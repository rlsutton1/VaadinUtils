package au.com.vaadinutils.crud.splitFields;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class SplitLabel extends Label implements SplitField
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5824753575628319814L;

	public SplitLabel(String content)
	{
		super(content);
	}

	public SplitLabel(String content, ContentMode contentMode)
	{
		super(content, contentMode);
	}

	@Override
	public Label getLabel()
	{
		return new Label(this, this.getContentMode());
	}
	@Override
	public void hideLabel()
	{
		
	}
	
	public String getCaption()
	{
		return this.getValue(); 
	}
}
