package au.com.vaadinutils.crud.adaptor;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Component;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.crud.ChildCrudView;
import au.com.vaadinutils.crud.CrudAction;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.HeadingPropertySet;
import au.com.vaadinutils.crud.ValidatingFieldGroup;

/**
 * this adaptor allows a class to work with a child crud without actually
 * extending the childcrud.
 * 
 * because this class implements CrudAdaptor, a class can behave both as a
 * childCrud and parentCrud
 * 
 * @author rsutton
 *
 * @param
 * 			<P>
 * @param <E>
 */
public class ChildCrudAdaptor<P extends CrudEntity, E extends ChildCrudEntity> extends ChildCrudView<P, E>
		implements CrudAdaptor<P, E>
{
	private static final long serialVersionUID = 1L;
	private ChildCrudInterface<P, E> client;

	public ChildCrudAdaptor(ChildCrudInterface<P, E> client, BaseCrudView<P> parent, Class<P> parentType,
			Class<E> childType, SingularAttribute<? extends CrudEntity, ? extends Object> parentKey,
			SingularAttribute<? extends CrudEntity, ? extends Object> childKey)
	{
		super(parent, parentType, childType, parentKey, childKey);

		this.client = client;

	}

	@Override
	protected Component buildEditor(ValidatingFieldGroup<E> fieldGroup2)
	{
		return client.buildEditor(fieldGroup2);
	}

	@Override
	protected Filter getContainerFilter(String filterString, boolean advancedSearchActive)
	{
		return client.getContainerFilter(filterString, advancedSearchActive);
	}

	@Override
	public void disallowNew(boolean disallow)
	{
		super.disallowNew(disallow);
	}

	@Override
	public void init(Class<E> entityClass, JPAContainer<E> container, HeadingPropertySet<E> headings)
	{
		super.init(entityClass, container, headings);
	}

	@Override
	public void setSplitPosition(float pos)
	{
		super.setSplitPosition(pos);
	}

	@Override
	public void disallowDelete(boolean disallow)
	{
		super.disallowDelete(disallow);
	}

	@Override
	public String getTitleText()
	{
		return client.getTitleText();
	}

	@Override
	public List<CrudAction<E>> getCrudActions()
	{
		return client.getCrudActions();
	}

	@Override
	public E getCurrent()
	{
		return super.getCurrent();
	}

	@Override
	public void createNewEntity(E previousEntity) throws InstantiationException, IllegalAccessException
	{
		super.createNewEntity(previousEntity);
		newEntity= client.createNewEntity(newEntity,previousEntity);
	}

	@Override
	public E preNew(E previousEntity) throws InstantiationException, IllegalAccessException
	{
		return client.preNew(super.preNew(previousEntity), previousEntity);
	}

	@Override
	public void rowChanged(EntityItem<E> item)
	{
		super.rowChanged(item);
		client.rowChanged(item);
	}

	@Override
	protected void postNew(EntityItem<E> newEntity)
	{
		client.postNew(newEntity);
	}

	@Override
	protected void interceptSaveValues(EntityItem<E> entityItem) throws Exception
	{
		client.interceptSaveValues(entityItem);
	}

	@Override
	protected void resetFilters()
	{
		super.resetFilters();
		client.resetFilters(container);
	}

	@Override
	public SingularAttribute<E, String> getGuidAttribute()
	{
		return client.getGuidAttribute();

	}

	@Override
	public void associateChild(P newParent, E child)
	{
		client.associateChild(newParent, child);

	}

	@Override
	public String getNewButtonActionLabel()
	{
		return client.getNewButtonActionLabel();
	}

	@Override
	public void selectedParentRowChanged(EntityItem<P> item)
	{
		super.selectedParentRowChanged(item);
		client.selectedParentRowChanged(item);
	}

	@Override
	public void setSplitPosition(int normalSplitSize)
	{
		super.setSplitPosition(normalSplitSize);

	}

	@Override
	public List<CrudAction<E>> getDefaultCrudActions()
	{
		return super.getCrudActions();
	}

}
