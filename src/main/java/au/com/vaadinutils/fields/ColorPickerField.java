package au.com.vaadinutils.fields;

import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.components.colorpicker.ColorChangeEvent;
import com.vaadin.ui.components.colorpicker.ColorChangeListener;

public class ColorPickerField extends CustomField<Color>
{
	private static final long serialVersionUID = -1573292123807845727L;

	private ColorPicker colorPicker;

	public ColorPickerField()
	{
		this.colorPicker = new ColorPicker();
		this.colorPicker.addColorChangeListener(new ColorChangeListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void colorChanged(ColorChangeEvent event)
			{
				setValue(new Color(event.getColor()));
			}
		});

	}

	@Override
	protected Component initContent()
	{
		return this.colorPicker;
	}

	@Override
	public Class<? extends Color> getType()
	{
		return Color.class;
	}

	
	@Override
	public Color getInternalValue()
	{
		return new Color(this.colorPicker.getColor());
	}
	
	@Override
	public void setInternalValue(Color newFieldValue)
	{
		this.colorPicker.setColor(new com.vaadin.shared.ui.colorpicker.Color(newFieldValue.getRed(), newFieldValue.getGreen(),
				newFieldValue.getBlue(), newFieldValue.getAlpha()));
		super.setInternalValue(newFieldValue);
	}

}
