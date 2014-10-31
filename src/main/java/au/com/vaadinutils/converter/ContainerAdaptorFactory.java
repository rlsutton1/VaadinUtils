package au.com.vaadinutils.converter;

import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container;

public class ContainerAdaptorFactory<T extends CrudEntity>
{

    @SuppressWarnings("unchecked")
    public static<T extends CrudEntity> ContainerAdaptor<T > getAdaptor(Container container)
    {
	if (container instanceof JPAContainer)
	{
	    return new ContainerAdaptorJPA<T>((JPAContainer<T>) container);
	}

	if (container instanceof EntityContainer)
	{
	    return new ContainerAdaptorEntity<T>((EntityContainer<T>) container);
	}
	throw new RuntimeException("Unknown container type");
    }

}
