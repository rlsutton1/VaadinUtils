package au.com.vaadinutils.jasper.ui;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;

public interface SplitPanel
{

	Component getComponent();

	void setSplitPosition(int i);

	void setFirstComponent(AbstractComponent optionsPanel);

	void setSecondComponent(AbstractComponent splash);

}
