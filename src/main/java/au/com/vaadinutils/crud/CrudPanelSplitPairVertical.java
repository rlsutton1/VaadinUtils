package au.com.vaadinutils.crud;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalSplitPanel;

public class CrudPanelSplitPairVertical extends VerticalSplitPanel implements CrudPanelPair
{

	
	private static final long serialVersionUID = 2032696860142886923L;



	@Override
	public Component getPanel()
	{

		return this;
	}

	@Override
	public void showFirstComponet()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showSecondComponet()
	{
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void setSplitPosition(float pos)
	{
		super.setSplitPosition(pos);
		
	}

	@Override
	public void setLocked(boolean locked)
	{
		super.setLocked(locked);
		
	}

	
}
