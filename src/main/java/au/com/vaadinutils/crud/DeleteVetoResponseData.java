package au.com.vaadinutils.crud;

import com.google.common.base.Preconditions;

public class DeleteVetoResponseData
{
	final public String message;
	final public boolean canDelete;

	public DeleteVetoResponseData(boolean canDelete)
	{
		Preconditions.checkArgument(canDelete, "Supply a message if vetoing the delete");
		message = "";
		this.canDelete = true;
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