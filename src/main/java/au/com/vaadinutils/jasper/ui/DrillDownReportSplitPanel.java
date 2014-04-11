package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalSplitPanel;

public class DrillDownReportSplitPanel extends VerticalSplitPanel implements SplitPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -470983852015042137L;

	DrillDownReportSplitPanel()
	{
		super.setSizeFull();
	}
	
	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSplitPosition(int i)
	{
		super.setSplitPosition(i);
		
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
