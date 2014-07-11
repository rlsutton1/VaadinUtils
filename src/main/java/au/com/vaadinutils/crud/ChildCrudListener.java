package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;

public interface ChildCrudListener<E>
{

	/**
	 * this method is invoked when the parent saves, signalling the children
	 * that they too should save. The parent entity is provided so that the child
	 * crud can retrieve the parent key (for new records)
	 * @throws Exception 
	 */
	public void committed(E newEntity) throws Exception;

	/**
	 * called by the parent when the parent changes row, allowing the child to
	 * change the set of records it is displaying to match the parent
	 * 
	 * @param item
	 */
	public void selectedParentRowChanged(EntityItem<E> parent);

	/**
	 * the parent crud calls this method to check if the child has changes
	 * 
	 * @return
	 */
	public boolean isDirty();

	public void validateFieldz();

	public void discard();

	public void saveEditsToTemp() throws Exception;

}
