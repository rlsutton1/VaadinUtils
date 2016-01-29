package au.com.vaadinutils.crud.adaptor.example;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudAction;
import au.com.vaadinutils.crud.ValidatingFieldGroup;
import au.com.vaadinutils.crud.adaptor.BaseCrudAdaptor;
import au.com.vaadinutils.crud.adaptor.ChildCrudAdaptor;
import au.com.vaadinutils.crud.adaptor.ChildCrudInterface;
import au.com.vaadinutils.crud.adaptor.CrudAdaptor;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity_;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduledDateParameter;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduledDateParameter_;

/**
 * this class can be either a child crud or a basecrud for the type
 * ReportEmailScheduledDateParameter
 * 
 * @author rsutton
 *
 */
public class ExampleCrud extends VerticalLayout
		implements ChildCrudInterface<ReportEmailScheduleEntity, ReportEmailScheduledDateParameter>

{

	private static final long serialVersionUID = 1L;
	private CrudAdaptor<?, ReportEmailScheduledDateParameter> crud;

	/**
	 * construct a child crud
	 * 
	 * @param parent
	 */
	ExampleCrud(BaseCrudView<ReportEmailScheduleEntity> parent)
	{
		crud = createChildCrud(parent);
	}

	/**
	 * construct a parent crud
	 */
	ExampleCrud()
	{
		crud = createParentCrud();
	}

	CrudAdaptor<?, ReportEmailScheduledDateParameter> createChildCrud(BaseCrudView<ReportEmailScheduleEntity> parent)
	{
		return new ChildCrudAdaptor<>(this, parent,
				ReportEmailScheduleEntity.class, ReportEmailScheduledDateParameter.class,
				ReportEmailScheduleEntity_.iID, ReportEmailScheduledDateParameter_.iID);

	}

	CrudAdaptor<?, ReportEmailScheduledDateParameter> createParentCrud()
	{

		return new BaseCrudAdaptor<>(this);

	}

	@Override
	public Component buildEditor(ValidatingFieldGroup<ReportEmailScheduledDateParameter> fieldGroup2)
	{
		return new VerticalLayout();
	}

	@Override
	public Filter getContainerFilter(String filterString, boolean advancedSearchActive)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitleText()
	{
		return "Example child/base crud";
	}

	@Override
	public List<CrudAction<ReportEmailScheduledDateParameter>> getCrudActions()
	{
		return crud.getDefaultCrudActions();
	}

	@Override
	public  EntityItem<ReportEmailScheduledDateParameter> createNewEntity(EntityItem<ReportEmailScheduledDateParameter> newEntity,
			ReportEmailScheduledDateParameter previousEntity) throws InstantiationException, IllegalAccessException
	{
		return newEntity;
	}

	@Override
	public ReportEmailScheduledDateParameter preNew(ReportEmailScheduledDateParameter newEntity,
			ReportEmailScheduledDateParameter previousEntity)
	{
		return newEntity;
	}

	@Override
	public void rowChanged(EntityItem<ReportEmailScheduledDateParameter> item)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postNew(EntityItem<ReportEmailScheduledDateParameter> newEntity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void interceptSaveValues(EntityItem<ReportEmailScheduledDateParameter> entityItem) throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void resetFilters(JPAContainer<ReportEmailScheduledDateParameter> container)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public SingularAttribute<ReportEmailScheduledDateParameter, String> getGuidAttribute()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void associateChild(ReportEmailScheduleEntity newParent, ReportEmailScheduledDateParameter child)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getNewButtonActionLabel()
	{
		return "Create New Date Parameter";
	}

	@Override
	public void selectedParentRowChanged(EntityItem<ReportEmailScheduleEntity> item)
	{
		// TODO Auto-generated method stub

	}

}
