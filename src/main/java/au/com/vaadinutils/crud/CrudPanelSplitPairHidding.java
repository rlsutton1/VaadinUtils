package au.com.vaadinutils.crud;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class CrudPanelSplitPairHidding extends VerticalLayout implements CrudPanelPair
{
	
	private static final long serialVersionUID = -3273324048011746886L;
	VerticalLayout firstPanel = new VerticalLayout();
	VerticalLayout secondPanel = new VerticalLayout();
	
	CrudPanelSplitPairHidding()
	{
		addComponent(firstPanel);
	}
	
	@Override
	public void setFirstComponent(Component c)
	{
		firstPanel.addComponent(c);
		
	}

	@Override
	public void setSecondComponent(Component c)
	{
		secondPanel.addComponent(c);
		
	}

	@Override
	public Component getPanel()
	{

		return this;
	}

	@Override
	public void setSplitPosition(float pos)
	{
		
		
	}

	@Override
	public void showFirstComponet()
	{
		removeAllComponents();
		addComponent(firstPanel);
	}

	@Override
	public void showSecondComponet()
	{
		removeAllComponents();
		addComponent(secondPanel);
	}

	@Override
	public void setLocked(boolean locked)
	{
		// TODO Auto-generated method stub
		
	}

	
}
