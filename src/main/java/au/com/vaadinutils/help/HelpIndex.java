package au.com.vaadinutils.help;

import com.vaadin.ui.AbstractLayout;

public interface HelpIndex
{

//	String lookupHelpIndex(Enum<?> helpId) throws ExecutionException;
//
//	String getPageUrl();
//
//	String getIndexPageUrl();

	

	void setHelpSource(Enum<?> helpId, AbstractLayout layout, HelpDisplayedCallback callback);

}
