package au.com.vaadinutils.crud;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.ui.Table.ColumnGenerator;

public class HeadingToPropertyId<E>
{
	private final String heading;
	private final String propertyId;
	private final ColumnGenerator columnGenerator;
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
	HeadingToPropertyId(final String heading, final String propertyId, final ColumnGenerator columnGenerator,
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

	public HeadingToPropertyId<E> setVisibleByDefault(final boolean defaultVisibleState)
	{
		this.defaultVisibleState = defaultVisibleState;
		return this;
	}

	public HeadingToPropertyId<E> setLocked()
	{
		lockedState = true;
		return this;
	}

	public HeadingToPropertyId<E> setWidth(final Integer width)
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

	public ColumnGenerator getColumnGenerator()
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
