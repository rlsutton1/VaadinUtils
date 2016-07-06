package au.com.vaadinutils.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.CrudEntity;

public class Path
{
	private List<Attribute<? extends CrudEntity, ? extends Object>> entities = new ArrayList<Attribute<? extends CrudEntity, ? extends Object>>();
	private String transientField;

	public Path()
	{

	}

	public Path(SingularAttribute<? extends CrudEntity, ? extends CrudEntity> lhsEntity,
			SingularAttribute<? extends CrudEntity, ? extends Object> rhsField)
	{
		this.entities.add(lhsEntity);
		this.entities.add(rhsField);
	}

	@SafeVarargs
	public Path(Attribute<? extends CrudEntity, ? extends Object>... entities)
	{
		if (!(entities[entities.length - 1] instanceof SingularAttribute))
		{
			throw new IllegalStateException("Last argument must be a SingularAttribute");
		}

		for (Attribute<? extends CrudEntity, ? extends Object> entity : entities)
		{
			this.entities.add(entity);
		}
	}

	/**
	 * Invalidates any cached entities of the classes within the
	 * SingularAttributes. The first class is ignored as this is the parent,
	 * which should be dealt with elsewhere
	 * 
	 * @return the Path
	 */
	public Path evict()
	{
		boolean first = true;
		for (Attribute<? extends CrudEntity, ? extends Object> entity : entities)
		{
			if (!first)
			{
				EntityManagerProvider.getEntityManager().getEntityManagerFactory().getCache()
						.evict(entity.getDeclaringType().getJavaType());
			}
			else
			{
				first = false;
			}
		}

		return this;
	}

	public String getName()
	{
		String path = new String();
		for (Attribute<? extends CrudEntity, ? extends Object> entity : entities)
		{
			if (path.length() > 0)
			{
				path += ".";
			}
			path += entity.getName();
		}

		if (transientField != null)
		{
			if (path.length() > 0)
			{
				path += ".";
			}
			path += transientField;
		}

		return path;
	}

	public Path add(SingularAttribute<? extends CrudEntity, ? extends Object> entity)
	{
		if (transientField != null)
		{
			throw new IllegalStateException("A transient field has already been added");
		}

		this.entities.add(entity);

		return this;
	}

	/**
	 * Adds a transient field to the Path. This method can only be called once
	 * on a Path.
	 *
	 * @param entity
	 *            the transient field name
	 * @return the Path
	 */
	public Path addTransient(String entity)
	{
		if (transientField != null)
		{
			throw new IllegalStateException("A transient field has already been added");
		}

		this.transientField = entity;

		return this;
	}

}
