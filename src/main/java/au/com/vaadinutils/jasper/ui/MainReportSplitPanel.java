package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class MainReportSplitPanel extends HorizontalLayout implements SplitPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -470983852015042137L;
	private VerticalLayout panela;
	private VerticalLayout panelb;

	MainReportSplitPanel(int width)
	{
		this.setSizeFull();
		panela =  new VerticalLayout();
		panela.setSizeFull();
		
		setSplitPosition(width);
		panelb =  new VerticalLayout();
		panelb.setSizeFull();
		this.addComponent(panela);
		this.addComponent(panelb);
		this.setExpandRatio(panelb, 1);
		
	}
	
	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSplitPosition(int i)
	{
		panela.setWidth(""+i);
		
	}

	@Override
	public void setFirstComponent(AbstractComponent optionsPanel)
	{
		panela.removeAllComponents();
		panela.addComponent(optionsPanel);
		
	}

	@Override
	public void setSecondComponent(AbstractComponent splash)
	{
		panelb.removeAllComponents();
		panelb.addComponent(splash);
		
	}

	

}
