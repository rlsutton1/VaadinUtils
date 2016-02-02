package au.com.vaadinutils.help;

import com.vaadin.ui.VerticalLayout;

public interface HelpPageListener extends HelpPageListenerMinimal
{

	public void showHelpOnPage();

	public void showHelpOnLayout(VerticalLayout layout);

	public void resetHelpPosition();
}
