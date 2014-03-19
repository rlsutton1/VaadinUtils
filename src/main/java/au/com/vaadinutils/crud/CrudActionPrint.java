package au.com.vaadinutils.crud;

import net.sf.jasperreports.engine.JRException;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.PrintWindow;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

public abstract class CrudActionPrint<E extends CrudEntity> implements CrudAction< E >
{
	private static final long serialVersionUID = 1L;
	private boolean isDefault = false;
	
	@Override
	public void exec(final BaseCrudView<E> crud,EntityItem<E> entity)
	{
		JasperManager manager;
		try
		{
			manager = prepareReport(entity);
			
			PrintWindow window = new PrintWindow(manager);
			
			UI.getCurrent().addWindow(window);
		}
		catch (JRException e)
		{
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}
	}

	
	abstract protected JasperManager prepareReport(EntityItem<E> entity) throws JRException;
	

	public String toString()
	{
		return "Print";
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
