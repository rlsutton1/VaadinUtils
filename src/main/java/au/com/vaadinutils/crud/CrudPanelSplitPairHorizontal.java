package au.com.vaadinutils.crud;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;

public class CrudPanelSplitPairHorizontal extends HorizontalSplitPanel implements CrudPanelPair
{

	
	
	private static final long serialVersionUID = 8705453622694076780L;

	@Override
	public Component getPanel()
	{

		return this;
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
	
	@Override
	public void showFirstComponet()
	{
		
	}

	@Override
	public void showSecondComponet()
	{
		
	}
}
