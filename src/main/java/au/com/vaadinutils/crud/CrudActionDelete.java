package au.com.vaadinutils.crud;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.ui.UI;

public class CrudActionDelete<E > implements CrudAction< CrudEntity>
{
	private boolean isDefault = true;
	
	@Override
	public void exec(final BaseCrudView<CrudEntity> crud,EntityItem<CrudEntity> entity)
	{
		ConfirmDialog.show(UI.getCurrent(), "Confirm Delete",
				"Are you sure you want to delete " + entity.getEntity().getName()+"?", "Delete", "Cancel",
				new ConfirmDialog.Listener()
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
