package au.com.vaadinutils.crud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

public class CrudActionDelete<E extends CrudEntity> implements CrudAction<E>
{
	private static final long serialVersionUID = 1L;
	private boolean isDefault = true;
	private DeleteAction<E> action;
	private String message;

	Logger logger = LogManager.getLogger();

	public CrudActionDelete()
	{
		this("");
	}

	/**
	 * 
	 * @param message
	 *            - provide additional information to be displayed in the delete
	 *            confirm dialog
	 */
	public CrudActionDelete(String message)
	{
		this(message, null);
	}

	/**
	 * 
	 * @param message
	 *            - provide additional information to be displayed in the delete
	 *            confirm dialog
	 * @param action
	 *            - perform additional cleanup like deleting files, called
	 *            before the entity is deleted.
	 */
	public CrudActionDelete(String message, DeleteAction<E> action)
	{
		this.action = action;
		this.message = message;
	}

	@Override
	public void exec(final BaseCrudView<E> crud, final EntityItem<E> entity)
	{
		DeleteVetoResponseData response = crud.canDelete(entity.getEntity());
		if (response.canDelete)
		{

			ConfirmDialog.show(UI.getCurrent(), "Confirm Delete", "Are you sure you want to delete "
					+ entity.getEntity().getName() + "? " + message, "Delete", "Cancel", new ConfirmDialog.Listener()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClose(ConfirmDialog dialog)
				{
					if (dialog.isConfirmed())
					{
						if (action != null)
						{
							try
							{
								action.delete(entity);
							}
							catch (Exception e)
							{
								logger.error(e, e);
								Notification.show("Errors occurred when deleting " + e.getMessage(), Type.ERROR_MESSAGE);
							}
						}
						crud.delete();
					}
				}

			});
		}
		else
		{
			ConfirmDialog dialog = ConfirmDialog.show(UI.getCurrent(), "Cannot Delete", response.getMessage(), "OK",
					"Cance", new ConfirmDialog.Listener()
					{
						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(ConfirmDialog dialog)
						{

						}

					});
			dialog.getCancelButton().setVisible(false);
			dialog.center();

		}

	}

	public String toString()
	{
		return "Delete";
	}

	public boolean isDefault()
	{
		return isDefault;
	}

	public void setIsDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
	}

	@Override
	public boolean showPreparingDialog()
	{
		return false;
	}
}
