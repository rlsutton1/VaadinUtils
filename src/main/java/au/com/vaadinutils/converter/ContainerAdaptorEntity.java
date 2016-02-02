package au.com.vaadinutils.converter;

import java.util.Collection;

import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class ContainerAdaptorEntity<E extends CrudEntity> implements ContainerAdaptor<E>
{

    private EntityContainer<E> container;

    public ContainerAdaptorEntity(EntityContainer<E> container)
    {
	this.container = container;
    }

    @Override
    public Item getItem(Object id)
    {
	return container.getItem(id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Property getProperty(E entity, Object id)
    {
	return container.getContainerProperty(entity.getId(), id);
    }

    @Override
    public E getEntity(Object id)
    {
	if (container.getItemIds().contains(id))
	{
	    return container.getEntity(id);
	}
	return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Collection<Object> getSortableContainerPropertyIds()
    {
	return (Collection<Object>) container.getSortableContainerPropertyIds();
    }

    @Override
    public void sort(String[] propertyId, boolean[] ascending)
    {
	container.sort(propertyId, ascending);
	
    }

    @Override
    public void removeAllContainerFilters()
    {
	container.removeAllContainerFilters();
	
    }

    @Override
    public void addContainerFilter(Filter filter)
    {
	container.addContainerFilter(filter);
	
    }

	@Override
	public Class<E> getEntityClass()
	{
		//TODO:
		return null;
	}
}
