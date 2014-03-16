package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;


public class Path
{
	List<SingularAttribute<? extends CrudEntity, ? extends Object>> entities = new ArrayList<SingularAttribute<? extends CrudEntity, ? extends Object>>();
	

	public Path()
	{
		
	}
	public   Path(SingularAttribute<? extends CrudEntity, ? extends CrudEntity > lhsEntity, SingularAttribute<? extends CrudEntity, ? extends Object> rhsField)
	{
		this.entities.add(lhsEntity);
		this.entities.add(rhsField);
	}
	
	public   Path(SingularAttribute<? extends CrudEntity, ? extends CrudEntity >... entities)
	{
		for (SingularAttribute<? extends CrudEntity, ? extends CrudEntity> entity : entities)
		{
			this.entities.add(entity);
		}
	}

	
	public String getName()
	{
		String path = new String();
		for (SingularAttribute<? extends CrudEntity, ? extends Object> entity : entities)
		{
			if (path.length() > 0)
				path += ".";
			path += entity.getName();
		}
		return path;
	}

	public Path add(SingularAttribute<? extends CrudEntity, ? extends Object > entity)
	{
		this.entities.add(entity);
		
		return this;
	}

}

