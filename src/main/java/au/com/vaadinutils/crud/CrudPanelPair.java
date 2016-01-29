package au.com.vaadinutils.crud;

import com.vaadin.ui.Component;

public interface CrudPanelPair
{

	public void setFirstComponent(Component c);

	public void setSecondComponent(Component c);

	public Component getPanel();

	public void setSplitPosition(float pos);

	public void showFirstComponet();

	public void showSecondComponet();

	void setLocked(boolean locked);

}
