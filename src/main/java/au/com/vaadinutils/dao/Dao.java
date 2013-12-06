package au.com.vaadinutils.dao;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

public interface Dao<E, K>
{
	void persist(E entity);
	
	public E merge(E entity);

	void remove(E entity);

	E findById(K id);
	
	List<E> findAll();

	List<E> findAll(SingularAttribute<E, ?> order[]);
}