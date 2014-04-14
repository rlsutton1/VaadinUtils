package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class DrillDownReportSplitPanel extends VerticalLayout implements SplitPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -470983852015042137L;
	private VerticalLayout top;
	private VerticalLayout bottom;

	DrillDownReportSplitPanel()
	{
		super.setSizeFull();
		top = new VerticalLayout();
		top.setSizeFull();
		top.setStyleName(Reindeer.LAYOUT_BLUE);

		top.setHeight("40");
		addComponent(top);
		
		bottom = new VerticalLayout();
		addComponent(bottom);
		bottom.setSizeFull();
		this.setExpandRatio(bottom, 1);
	}
	
	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSplitPosition(int i)
	{
		
		
	}

	@Override
	public void setFirstComponent(AbstractComponent optionsPanel)
	{
		top.removeAllComponents();
		top.addComponent(optionsPanel);
		
	}

	@Override
	public void setSecondComponent(AbstractComponent splash)
	{
		bottom.removeAllComponents();
		bottom.addComponent(splash);
		
	}

	

}
