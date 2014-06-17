package au.com.vaadinutils.crud;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.ui.UI;

public class CrudActionDelete<E extends CrudEntity> implements CrudAction<E>
{
	private static final long serialVersionUID = 1L;
	private boolean isDefault = true;

	public CrudActionDelete()
	{
	}

	@Override
	public void exec(final BaseCrudView<E> crud, EntityItem<E> entity)
	{
		DeleteVetoResponseData response = crud.canDelete(entity.getEntity());
		if (response.canDelete)
		{

			ConfirmDialog.show(UI.getCurrent(), "Confirm Delete", "Are you sure you want to delete "
					+ entity.getEntity().getName() + "?", "Delete", "Cancel", new ConfirmDialog.Listener()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClose(ConfirmDialog dialog)
				{
					if (dialog.isConfirmed())
					{

						crud.delete();
					}
				}

			});
		}else
		{
			ConfirmDialog dialog = ConfirmDialog.show(UI.getCurrent(), "Cannot Delete", response.getMessage(), "OK", "Cance", new ConfirmDialog.Listener()
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
}
