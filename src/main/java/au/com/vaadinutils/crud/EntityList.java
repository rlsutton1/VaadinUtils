package au.com.vaadinutils.crud;

import com.vaadin.ui.Component;

public interface EntityList<E> extends Component 
{

	public void setRowChangeListener(RowChangeListener<E> listener);

	public void setSortEnabled(boolean b);

	public Object firstItemId();

	public void init();

	public void select(Object firstItemId);

	public Object getValue();


	public boolean removeItem(Object contactId);

	public Object getCurrentPageFirstItemId();

	public Object prevItemId(Object contactId);
}
