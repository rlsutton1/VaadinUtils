package au.com.vaadinutils.help;

import com.vaadin.ui.AbstractOrderedLayout;

public interface HelpIndex
{

	void setHelpSource(Enum<?> helpId, AbstractOrderedLayout layout, HelpDisplayedCallback callback);

}
