package au.com.vaadinutils.converter;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container;

import au.com.vaadinutils.crud.CrudEntity;

public class ContainerAdaptorFactory<T extends CrudEntity>
{

	@SuppressWarnings("unchecked")
	public static <T extends CrudEntity> ContainerAdaptor<T> getAdaptor(Container container)
	{
		if (container instanceof JPAContainer)
		{
			return new ContainerAdaptorJPA<T>((JPAContainer<T>) container);
		}

		throw new RuntimeException("Unknown container type");
	}

}
