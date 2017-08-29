package au.com.vaadinutils.layout;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

public class TopVerticalLayout extends CustomComponent
{

	private static final long serialVersionUID = 7166258936287790784L;

	private VerticalLayout container = new VerticalLayout();
	private VerticalLayout contents = new VerticalLayout();
	private VerticalLayout spacer = new VerticalLayout();

	public TopVerticalLayout()
	{
		setCompositionRoot(container);
		container.addComponent(contents);
		container.addComponent(spacer);

		contents.setSizeFull();
		spacer.setSizeFull();

		container.setExpandRatio(spacer, 1);

	}

	public TopVerticalLayout(Component... children)
	{
		this();
		addComponents(children);
	}

	public void addComponents(Component... components)
	{
		for (Component c : components)
		{
			addComponent(c);
		}
	}

	public void addComponent(Component component)
	{
		contents.addComponent(component);
	}

	public void removeComponent(Component component)
	{
		contents.removeComponent(component);
	}

	public void setMargin(boolean b)
	{
		container.setMargin(b);
	}

	public void setMargin(MarginInfo marginInfo)
	{
		container.setMargin(marginInfo);
	}
}
