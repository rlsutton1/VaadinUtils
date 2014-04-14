package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;

public class MainReportSplitPanel extends HorizontalSplitPanel implements SplitPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -470983852015042137L;

	MainReportSplitPanel()
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
