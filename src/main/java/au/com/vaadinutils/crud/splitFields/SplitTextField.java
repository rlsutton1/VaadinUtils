package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class SplitTextField extends TextField implements SplitField
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7753660388792217050L;
	private Label label;

	public SplitTextField(String label)
	{
		this.label = new Label(label);
		setCaption(label);

	}

	/**
	 * trim all leading and trailing white space from text feilds
	 */
	@Override
	public String getValue()
	{
		String tmp = super.getValue();
		if (tmp != null)
		{
			tmp = tmp.trim();
		}
		return tmp;
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
