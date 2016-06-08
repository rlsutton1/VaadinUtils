package au.com.vaadinutils.crud;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.data.util.PropertyValueGenerator;

public class GridHeadingToPropertyId<E>
{
	private final String heading;
	private final String propertyId;
	private final PropertyValueGenerator<?> columnGenerator;
	private Integer width;
	private boolean defaultVisibleState = true;
	private boolean lockedState = false;

	/**
	 * Instantiates a new heading to property id.
	 *
	 * @param heading
	 *            the column heading that will be displayed
	 * @param headingPropertyId
	 *            the heading property id
	 * @param columnGenerator
	 *            the column generator
	 * @param defaultVisibleState
	 *            whether the column is visible by default
	 * @param lockedState
	 *            whether the visibility of a column can be modified
	 * @param width
	 *            the width of the column
	 */
	GridHeadingToPropertyId(final String heading, final String propertyId, final PropertyValueGenerator<?> columnGenerator,
			final boolean defaultVisibleState, final boolean lockedState, final Integer width)
	{
		Preconditions.checkNotNull(propertyId);
		this.heading = heading;
		this.propertyId = propertyId;
		this.columnGenerator = columnGenerator;
		this.defaultVisibleState = defaultVisibleState;
		this.lockedState = lockedState;
		this.width = width;
	}

	public GridHeadingToPropertyId<E> setVisibleByDefault(final boolean defaultVisibleState)
	{
		this.defaultVisibleState = defaultVisibleState;
		return this;
	}

	public GridHeadingToPropertyId<E> setLocked()
	{
		lockedState = true;
		return this;
	}

	public GridHeadingToPropertyId<E> setWidth(final Integer width)
	{
		this.width = width;
		return this;
	}

	public String getPropertyId()
	{
		return propertyId;
	}

	public String getHeader()
	{
		return heading;
	}

	public PropertyValueGenerator<?> getColumnGenerator()
	{
		return columnGenerator;
	}

	/**
	 * returns true if the column is a virtual table column and not in the
	 * underlying container.
	 * 
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

	public boolean isVisibleByDefault()
	{
		return defaultVisibleState;
	}

	public boolean isLocked()
	{
		return lockedState;
	}
}
