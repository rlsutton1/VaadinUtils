package au.com.vaadinutils.crud.splitFields;

import au.com.vaadinutils.fields.CKEditorEmailField;

import com.vaadin.ui.Label;

public class SplitEditorField extends CKEditorEmailField implements SplitField
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7753660388792217050L;

	public SplitEditorField(boolean readonly)
	{
		super(readonly);
	}

	public SplitEditorField(boolean readonly,  ConfigModifier configModifier)
	{
		super(readonly,configModifier);
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
	}

	@Override
	public Label getLabel()
	{
		return null;
	}

	@Override
	public String getCaption()
	{
		return null;
	}
	@Override
	public void hideLabel()
	{
		setCaption(null);
		
	}

}
