package au.com.vaadinutils.dao;

import java.util.HashMap;

import javax.persistence.metamodel.SingularAttribute;

/**
 * This class is to be used with the find*By*Attributes methods in JpaBaseDao.
 * It is used for type checking of the singular attribute and the value to
 * search on.
 * 
 * @param <T> the class of the entity
 */
@SuppressWarnings("serial")
public class AttributesHashMap<T> extends HashMap<SingularAttribute<T, Object>, Object>
{
	@SuppressWarnings("unchecked")
	public <K> void safePut(SingularAttribute<T, K> key, K value)
	{
		super.put((SingularAttribute<T, Object>) key, value);
	}

	@Override
	public Object put(SingularAttribute<T, Object> key, Object value)
	{
		throw new RuntimeException("Use safePut method!");
	}

}
