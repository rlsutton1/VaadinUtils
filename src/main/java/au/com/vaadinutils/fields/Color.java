package au.com.vaadinutils.fields;

import javax.persistence.Embeddable;

/**
 * A wrapper class for the color pickers Color class so we can embed a Color
 * in a JPA Entity.
 * 
 * @author bsutton
 * 
 */
@Embeddable
public class Color
{
	private int red;
	private int green;
	private int blue;
	private int alpha;

	public Color()
	{
		red = 255;
		green = 255;
		blue = 255;
		alpha = 255;
	}
	public Color(com.vaadin.shared.ui.colorpicker.Color color)
	{
		setRed(color.getRed());
		setGreen(color.getGreen());
		setBlue(color.getBlue());
		setAlpha(color.getAlpha());
	}

	public String getCSS()
	{
		return new com.vaadin.shared.ui.colorpicker.Color(getRed(), getGreen(), getBlue(), getAlpha()).getCSS();
	}

	public int getRed()
	{
		return red;
	}

	public void setRed(int red)
	{
		this.red = red;
	}

	public int getGreen()
	{
		return green;
	}

	public void setGreen(int green)
	{
		this.green = green;
	}

	public int getBlue()
	{
		return blue;
	}

	public void setBlue(int blue)
	{
		this.blue = blue;
	}

	public int getAlpha()
	{
		return alpha;
	}

	public void setAlpha(int alpha)
	{
		this.alpha = alpha;
	}
	
}