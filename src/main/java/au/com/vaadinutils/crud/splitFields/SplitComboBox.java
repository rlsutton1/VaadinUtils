package au.com.vaadinutils.crud.splitFields;

import com.vaadin.data.Container;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;

public class SplitComboBox extends ComboBox implements SplitField
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6304106636121695094L;
	private Label label;

	public SplitComboBox(String label)
	{
		this.label = new Label(label);
		
	}

	public SplitComboBox(String fieldLabel, Container createContainerFromEnumClass)
	{

		super(null, createContainerFromEnumClass);
		this.label = new Label(fieldLabel);
		
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
