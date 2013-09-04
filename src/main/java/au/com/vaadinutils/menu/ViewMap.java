package au.com.vaadinutils.menu;

import java.io.Serializable;

import com.vaadin.navigator.View;

public class ViewMap implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String viewName;
	private Class<? extends View> view;

	public ViewMap(String viewName, Class<? extends View> class1)
	{
		this.setViewName(viewName);
		this.setView(class1);
	}

	public Class<? extends View> getView()
	{
		return view;
	}

	public void setView(Class<? extends View> view)
	{
		this.view = view;
	}

	public String getViewName()
	{
		return viewName;
	}

	public void setViewName(String viewName)
	{
		this.viewName = viewName;
	}
}