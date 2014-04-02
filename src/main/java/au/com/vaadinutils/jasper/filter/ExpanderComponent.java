package au.com.vaadinutils.jasper.filter;

import com.vaadin.ui.Component;

public class ExpanderComponent 
{
	final private Component component;
	final private boolean shouldExpand;

	ExpanderComponent(Component component, boolean shouldExpand)
	{
		this.component = component;
		this.shouldExpand = shouldExpand;
	}

	/**
	 * @return the component
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * @return the expandRatio
	 */
	public boolean shouldExpand()
	{
		return shouldExpand;
	}
	
}
