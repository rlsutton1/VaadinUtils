package au.com.vaadinutils.help;

public interface DisableHelpPolling
{

	/**
	 * if you implement this interface the HelpSplitPanel will not poll to check for changes in the size of the help page.
	 * 
	 * Particularly the CKEditor is sensitive to the polling and resets it's cursor position
	 */
}
