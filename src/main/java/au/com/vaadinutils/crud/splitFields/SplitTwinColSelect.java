package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TwinColSelect;

public class SplitTwinColSelect extends TwinColSelect implements SplitField
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7088825671520977496L;

	private Label label;
	
	public SplitTwinColSelect(String label)
	{
		this.label = new Label(label);
		setCaption(label);
	}

	@Override
	public Label getLabel()
	{
		return label;
	}

	@Override
	public void hideLabel()
	{
		setCaption(null);
	}

	@Override
	public String getCaption()
	{
		return label.getValue();
	}
}
