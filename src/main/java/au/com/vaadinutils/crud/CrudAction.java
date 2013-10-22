package au.com.vaadinutils.crud;

public interface CrudAction<E extends CrudEntity>
{
	public String toString();
	
	void exec(BaseCrudView<E> crud, E entity);
}
