package au.com.vaadinutils.help;


public interface HelpPageListener
{
	/**
	 * when a component wants a different help page displayed it calls this
	 * method providing the helpPageId
	 * 
	 * @param id
	 * 
	 * 
	 */
	public void setHelp(Enum<?> id);

	/**
	 * The HelpSplitPanel invokes this method on the component that is added to
	 * it. passing the HelpSplitPanel as the argument.
	 * 
	 * this way Compoents that implement HelpPageListener will be automatically
	 * provided with the HelpPageListener to pass through the calls to setHelp
	 * 
	 * @param helpSplitPanel
	 */
	public void setHelpPageListener(HelpPageListener helpSplitPanel);
	
	public void showHelpOnPage();
}
