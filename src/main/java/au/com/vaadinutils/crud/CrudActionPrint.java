package au.com.vaadinutils.crud;

import java.util.LinkedList;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import au.com.vaadinutils.jasper.JasperManager;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.ui.JasperReportPopUp;
import au.com.vaadinutils.jasper.ui.JasperReportProperties;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public abstract class CrudActionPrint<E extends CrudEntity> implements CrudAction<E>, JasperReportProperties
{
	private static final long serialVersionUID = 1L;
	private boolean isDefault = false;

	@Override
	public void exec(final BaseCrudView<E> crud, EntityItem<E> entity)
	{
		JasperManager manager;
		try
		{
			List<ReportParameter<?>> filters = new LinkedList<ReportParameter<?>>();
			new JasperReportPopUp(prepareReport(entity), filters);
		}
		catch (JRException e)
		{
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}
	}

	abstract protected JasperReportProperties prepareReport(EntityItem<E> entity) throws JRException;

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
