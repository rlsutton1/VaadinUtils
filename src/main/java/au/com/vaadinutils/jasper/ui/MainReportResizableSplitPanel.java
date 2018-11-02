package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;

public class MainReportResizableSplitPanel extends HorizontalSplitPanel implements SplitPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -470983852015042137L;

	MainReportResizableSplitPanel(int width)
	{
		this.setSizeFull();
		setSplitPosition(width);
		setMinSplitPosition(width, Unit.PIXELS);

	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSplitPosition(int i)
	{
		super.setSplitPosition(i, Unit.PIXELS);

	}

	@Override
	public void setFirstComponent(AbstractComponent optionsPanel)
	{
		super.setFirstComponent(optionsPanel);

	}

	@Override
	public void setSecondComponent(AbstractComponent splash)
	{
		super.setSecondComponent(splash);

	}

}
