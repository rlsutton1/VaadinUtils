package au.com.vaadinutils.crud;

import javax.persistence.metamodel.SingularAttribute;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.ui.Table.ColumnGenerator;

public class HeadingToPropertyId<E>
{
	private final String heading;
	private final String propertyId;
	private final ColumnGenerator columnGenerator;
	private final SingularAttribute<E, ?> attribute;
	private Integer width;
	private boolean hidden=false;

	/**
	 * 
	 * @param heading
	 * @param columnGenerator2 
	 * @param hidden TODO
	 * @param propertyId
	 *            - the real field name
	 */
	public <M extends Object> HeadingToPropertyId(String heading, SingularAttribute<E, M> attribute, ColumnGenerator columnGenerator2, Boolean hidden)
	{
		Preconditions.checkNotNull(attribute);
		this.heading = heading;
		this.propertyId = null;
		this.attribute = attribute;
		this.columnGenerator = columnGenerator2;
		this.hidden = hidden;
	}
	
	/**
	 * 
	 * @param heading
	 * @param propertyId
	 *            - the real field name
	 * @param columnGenerator2 
	 */
	public HeadingToPropertyId(String heading, String propertyId, ColumnGenerator columnGenerator2)
	{
		Preconditions.checkNotNull(propertyId);
		this.heading = heading;
		this.propertyId = propertyId;
		this.attribute = null;
		this.columnGenerator = columnGenerator2;
	}
	
	public HeadingToPropertyId<E> setWidth(Integer width)
	{
		this.width = width;
		return this;
	}


	public String getPropertyId()
	{
		return (propertyId == null ? this.attribute.getName() : this.propertyId);
	}

	public String getHeader()
	{
		return heading;
	}

	public ColumnGenerator getColumnGenerator()
	{

		return columnGenerator;
	}

	/**
	 * returns true if the column is a virtual table column and not in the underlying container.
	 * @return
	 */
	public boolean isGenerated()
	{
		return columnGenerator != null;
	}

	public Integer getWidth()
	{
		return width;
	}

	public boolean isHidden()
	{
		return hidden;
	}

}
