package au.com.vaadinutils.crud.splitFields;

import au.com.vaadinutils.fields.OrderingListSelect;

import com.vaadin.ui.Label;

public class SplitOrderingListSelect<T> extends OrderingListSelect<T> implements SplitField
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7088825671520977496L;

	private Label label;

	public SplitOrderingListSelect(String label)
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
