package au.com.vaadinutils.crud;

import java.util.LinkedList;
import java.util.List;

public class HeadingPropertySet
{
	List<HeadingToPropertyId> cols = new LinkedList<HeadingToPropertyId>();
	
	private HeadingPropertySet()
	{
		// use the builder!
	}
	
	public static class Builder
	{
		List<HeadingToPropertyId> cols = new LinkedList<HeadingToPropertyId>();
		public Builder addColumn(String heading, String propertyId)
		{
			cols.add(new HeadingToPropertyId(heading,propertyId));
			return this;
		}
		
		public HeadingPropertySet build()
		{
			HeadingPropertySet tmp = new HeadingPropertySet();
			tmp.cols = this.cols;
			return tmp;
		}
	}

	public List<HeadingToPropertyId> getColumns()
	{
		
		return cols;
	}
}
