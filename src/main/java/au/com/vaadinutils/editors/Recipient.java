package au.com.vaadinutils.editors;

public interface Recipient
{
	/** 
	 * 
	 * @param input
	 * @return true if the dialog should be closed
	 */
	public boolean onOK(String input);

	/**
	 * 
	 * @return true if the dialog should be closed
	 */
	public boolean onCancel();
}
