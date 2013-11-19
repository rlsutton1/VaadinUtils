package au.com.vaadinutils.fields;

public interface AutoCompleteParent<E>
{

	boolean hasEntity(E entity);

	void attachEntity(E entity);
	
	void detachEntity(E entity);
}
