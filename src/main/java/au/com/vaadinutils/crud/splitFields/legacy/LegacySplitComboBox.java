package au.com.vaadinutils.crud.splitFields.legacy;

import java.util.Collection;

import org.vaadin.ui.LegacyComboBox;

import com.vaadin.data.Container;
import com.vaadin.ui.Label;

import au.com.vaadinutils.crud.splitFields.SplitField;

public class LegacySplitComboBox extends LegacyComboBox implements SplitField
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3156478731788878472L;
	private Label label;

	public LegacySplitComboBox(String label)
	{
		this.label = new Label(label);
		setCaption(label);
	}

	public LegacySplitComboBox(String fieldLabel, Container createContainerFromEnumClass)
	{
		super(fieldLabel, createContainerFromEnumClass);
		this.label = new Label(fieldLabel);
	}

	public LegacySplitComboBox(String fieldLabel, Collection<?> options)
	{
		super(fieldLabel, options);
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
	
	@Override
	public void hideLabel()
	{
		setCaption(null);
	}

}
