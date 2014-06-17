package au.com.vaadinutils.crud;

import com.google.common.base.Preconditions;

public class DeleteVetoResponseData
{
	final public String message;
	final public boolean canDelete;

	public DeleteVetoResponseData(boolean b)
	{
		Preconditions.checkArgument(b, "Supply a message if vetoing the delete");
		message = "";
		canDelete = true;
	}

	public DeleteVetoResponseData(boolean canDelete, String message2)
	{
		message = message2;
		this.canDelete = canDelete;
	}

	public String getMessage()
	{
		return message;
	}
}