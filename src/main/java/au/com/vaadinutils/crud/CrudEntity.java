package au.com.vaadinutils.crud;

import java.io.Serializable;

public interface CrudEntity extends Serializable
{

	public Long getId();

	public void setId(Long id);

	/**
	 * used when displaying messages that need to identify an individual entity
	 * to the user
	 * 
	 * @return
	 */
	public String getName();
}
