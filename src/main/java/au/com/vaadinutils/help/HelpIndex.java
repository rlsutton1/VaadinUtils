package au.com.vaadinutils.help;

import com.vaadin.ui.AbstractOrderedLayout;

public interface HelpIndex
{

//	String lookupHelpIndex(Enum<?> helpId) throws ExecutionException;
//
//	String getPageUrl();
//
//	String getIndexPageUrl();

	

	void setHelpSource(Enum<?> helpId, AbstractOrderedLayout layout, HelpDisplayedCallback callback);

}
