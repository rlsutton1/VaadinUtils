package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class MainReportSplitPanel extends HorizontalLayout implements SplitPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -470983852015042137L;
	private VerticalLayout panela;
	private VerticalLayout panelb;
	private Panel wrapper;

	MainReportSplitPanel(int width)
	{
		this.setSizeFull();
		
		 wrapper = new Panel();
		 wrapper.setHeight("100%");
		 
		
		panela =  new VerticalLayout();
		panela.setSizeFull();
		wrapper.setContent(panela);
		wrapper.setId("Wrapper panel");
		panela.setId("PanelA");
		
		setSplitPosition(width);
		panelb =  new VerticalLayout();
		panelb.setSizeFull();
		this.addComponent(wrapper);
		this.addComponent(panelb);
		this.setExpandRatio(panelb, 1);
		this.setSizeFull();
		
	}
	
	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSplitPosition(int i)
	{
		wrapper.setWidth(""+(i+5));
		
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
