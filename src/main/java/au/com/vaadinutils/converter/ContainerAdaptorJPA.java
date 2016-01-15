package au.com.vaadinutils.converter;

import java.util.Collection;
import java.util.HashSet;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class ContainerAdaptorJPA<E extends CrudEntity> implements ContainerAdaptor<E>
{

    private JPAContainer<E> container;

    public ContainerAdaptorJPA(JPAContainer<E> containerDataSource)
    {
	container = containerDataSource;
    }

    @Override
    public Item getItem(Object id)
    {
	return container.getItem(id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Property getProperty(E item, Object propertyId)
    {
	return container.getContainerProperty(item.getId(), propertyId);
    }

    @Override
    public E getEntity(Object id)
    {
	return container.getItem(id).getEntity();
    }

    @Override
    public Collection<Object> getSortableContainerPropertyIds()
    {
	Collection<Object> ids = new HashSet<>();
	ids.addAll(container.getSortableContainerPropertyIds());
	return ids;
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
		return container.getEntityClass();
	}

}
