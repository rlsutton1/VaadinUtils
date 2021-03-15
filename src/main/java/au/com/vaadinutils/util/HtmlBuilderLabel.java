package au.com.vaadinutils.util;

import org.apache.commons.lang3.StringEscapeUtils;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class HtmlBuilderLabel extends Label
{
	private static final long serialVersionUID = 1L;

	public HtmlBuilderLabel()
	{
		super.setContentMode(ContentMode.HTML);
	}

	public HtmlBuilderLabel(String text)
	{
		super.setContentMode(ContentMode.HTML);
		super.setValue(StringEscapeUtils.escapeHtml4(text));
	}

	public HtmlBuilderLabel(HtmlBuilder builder)
	{
		super.setContentMode(ContentMode.HTML);
		super.setValue(builder.getSafeText());
	}

	@Deprecated
	@Override
	public void setContentMode(ContentMode contentMode)
	{
		super.setContentMode(contentMode);
	}

	/**
	 * do not use this, call build() to get an HtmlBuilder or directly instance
	 * an HtmlBuilder
	 */
	@Deprecated
	@Override
	public void setValue(String newStringValue)
	{
		super.setValue(newStringValue);
	}

	public void setValue(HtmlBuilder builder)
	{
		super.setValue(builder.getSafeText());
	}

	/**
	 * creates a new builder, and clears the contents of this label
	 * 
	 * @return HtmlBuilder
	 */
	public HtmlBuilder builder()
	{
		return new HtmlBuilder(this);
	}

}
