package au.com.vaadinutils.util;

import org.apache.commons.lang.StringEscapeUtils;

import com.vaadin.shared.ui.colorpicker.Color;

public class HtmlBuilder
{
	private String safeText = "";
	private HtmlBuilderLabel label;

	public HtmlBuilder()
	{
	}

	HtmlBuilder(HtmlBuilderLabel htmlBuilderLabel)
	{
		label = htmlBuilderLabel;
		label.setValue(this);
	}

	public HtmlBuilder(String text)
	{
		plain(text);
	}

	public HtmlBuilder bold(String text)
	{
		return add("<b>" + StringEscapeUtils.escapeHtml(text) + "</b>");

	}

	public HtmlBuilder boldRed(String text)
	{
		return boldColor(text, Color.RED);
	}

	public HtmlBuilder red(String text)
	{
		return color(text, Color.RED);
	}

	private HtmlBuilder add(String text)
	{
		safeText += text;
		return this;

	}

	public String getSafeText()
	{
		return safeText;
	}

	public HtmlBuilder plain(String text)
	{
		return add(StringEscapeUtils.escapeHtml(text));
	}

	public HtmlBuilder br()
	{
		return add("<br>");
	}

	public HtmlBuilder color(String text, Color color)
	{
		return add("<font color=" + color.getCSS() + ">" + StringEscapeUtils.escapeHtml(text) + "</font>");
	}

	public HtmlBuilder boldColor(String text, Color fontColor)
	{
		return add("<font color=" + fontColor.getCSS() + "><b>" + StringEscapeUtils.escapeHtml(text) + "</b></font>");
	}

	public HtmlBuilder greenBold(String text)
	{
		return boldColor(text, Color.GREEN);
	}

	public HtmlBuilder space()
	{
		return add("&nbsp");
	}

	public HtmlBuilder colorSize(String text, Color color, int size)
	{
		return add("<font size=\"" + size + "\"color=" + color.getCSS() + ">" + StringEscapeUtils.escapeHtml(text)
				+ "</font>");

	}

	public HtmlBuilder heading(String text, int size)
	{
		return add("<h" + size + ">" + StringEscapeUtils.escapeHtml(text) + "</h" + size + ">");
	}

	public HtmlBuilder headingColor(String text, int size, Color color)
	{
		return add("<h" + size + ">" + "<font size=\"" + size + "\"color=" + color.getCSS() + ">"
				+ StringEscapeUtils.escapeHtml(text) + "</font>" + "</h" + size + ">");

	}

	/**
	 * be carefull using this
	 * 
	 * @param html
	 * @return
	 */
	public HtmlBuilder html(String html)
	{
		return add(html);
	}

}
