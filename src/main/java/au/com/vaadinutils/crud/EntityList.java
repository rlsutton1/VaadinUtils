package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table.ColumnGenerator;

public interface EntityList<E> extends Component 
{

	public void setRowChangeListener(RowChangeListener<E> listener);

	public void setSortEnabled(boolean b);

	public Object firstItemId();

	public void init();

	public void select(Object firstItemId);

	public Object getValue();
	
	public EntityItem<E> getCurrent();


	public boolean removeItem(Object entityId);

	public Object getCurrentPageFirstItemId();

	public Object prevItemId(Object entityId);

	public void setColumnCollapsingAllowed(boolean b);

	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn);
}
