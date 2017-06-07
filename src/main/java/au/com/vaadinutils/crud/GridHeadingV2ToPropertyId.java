package au.com.vaadinutils.crud;

import com.google.common.base.Preconditions;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Grid.AbstractRenderer;

import au.com.vaadinutils.crud.GridHeadingV2PropertySet.WidthType;

public class GridHeadingV2ToPropertyId
{
	private final String heading;
	private final String propertyId;
	private final PropertyValueGenerator<?> columnGenerator;
	private Integer width;
	private WidthType widthType;
	private boolean visible = true;
	private boolean visibilityLocked = false;

	private AbstractRenderer<?> renderer = null;
	private Converter<String, ?> converter;

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
	 * @param visibilityLocked
	 *            whether the visibility of a column can be modified
	 * @param width
	 *            the width of the column
	 */
	GridHeadingV2ToPropertyId(final String heading, final String propertyId,
			final PropertyValueGenerator<?> columnGenerator, final boolean defaultVisibleState,
			final boolean visibilityLocked, final Integer width, final WidthType widthType)
	{
		Preconditions.checkNotNull(propertyId);
		this.heading = heading;
		this.propertyId = propertyId;
		this.columnGenerator = columnGenerator;
		this.visible = defaultVisibleState;
		this.visibilityLocked = visibilityLocked;
		this.width = width;
		this.widthType = widthType;
	}

	static final class Builder
	{
		private final String heading;
		private String propertyId;
		private PropertyValueGenerator<?> columnGenerator = null;
		private Integer width;
		private WidthType widthType = WidthType.FREE;
		private boolean visible = true;
		private boolean visibilityLocked = false;
		private AbstractRenderer<?> renderer = null;
		private Converter<String, ?> converter = null;

		Builder(String heading, String propertyId)
		{
			this.heading = heading;
			this.propertyId = propertyId;
		}

		GridHeadingV2ToPropertyId build()
		{
			GridHeadingV2ToPropertyId tmp = new GridHeadingV2ToPropertyId(heading, propertyId, columnGenerator, visible,
					visibilityLocked, width, widthType);
			tmp.setRenderer(renderer);
			tmp.setConverter(converter);
			return tmp;
		}

		public Builder setVisible(boolean visible, boolean visibilityLocked)
		{
			this.visible = visible;
			this.visibilityLocked = visibilityLocked;
			return this;
		}

		public Builder setWidth(Integer width, WidthType widthType)
		{
			this.width = width;
			this.widthType = widthType;
			return this;
		}

		public Builder setColumnGenerator(PropertyValueGenerator<?> columnGenerator)
		{
			if (columnGenerator == null)
			{
				return this;
			}

			this.columnGenerator = columnGenerator;
			if (propertyId == null)
			{
				propertyId = heading + "-generated";
			}

			return this;
		}

		public Builder setRenderer(AbstractRenderer<?> renderer)
		{
			this.renderer = renderer;
			return this;

		}

		public Builder setConverter(Converter<String, ?> converter)
		{
			this.converter = converter;
			return this;

		}

	}

	public GridHeadingV2ToPropertyId setVisible(final boolean visible)
	{
		this.visible = visible;
		return this;
	}

	public void setConverter(Converter<String, ?> converter)
	{
		this.converter = converter;

	}

	public void setRenderer(AbstractRenderer<?> renderer)
	{
		this.renderer = renderer;

	}

	public GridHeadingV2ToPropertyId setVisibilityLocked()
	{
		visibilityLocked = true;
		return this;
	}

	public GridHeadingV2ToPropertyId setWidth(final Integer width)
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

	public WidthType getWidthType()
	{
		return widthType;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public boolean isVisibilityLocked()
	{
		return visibilityLocked;
	}

	public AbstractRenderer<?> getRenderer()
	{
		return renderer;
	}

	public Converter<String, ?> getConverter()
	{
		return converter;
	}
}
