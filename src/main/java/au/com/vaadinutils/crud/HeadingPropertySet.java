package au.com.vaadinutils.crud;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.ui.Table.ColumnGenerator;

public class HeadingPropertySet<E>
{
	List<HeadingToPropertyId> cols = new LinkedList<HeadingToPropertyId>();
	
	private HeadingPropertySet()
	{
		// use the builder!
	}
	
	public static class Builder<E>
	{
		List<HeadingToPropertyId> cols = new LinkedList<HeadingToPropertyId>();
		
		public HeadingPropertySet<E> build()
		{
			HeadingPropertySet<E> tmp = new HeadingPropertySet<E>();
			tmp.cols = this.cols;
			return tmp;
		}

			
		public  Builder<E> addGeneratedColumn(String heading, String headingPropertyId,ColumnGenerator columnGenerator)
		{
			cols.add(new HeadingToPropertyId(heading,headingPropertyId,columnGenerator));
			return this;
			
		}
		public  Builder<E> addColumn(String heading, String headingPropertyId)
		{
			cols.add(new HeadingToPropertyId(heading,headingPropertyId,null));
			return this;
			
		}
		
		public <T extends Object> Builder<E> addColumn(String heading, SingularAttribute<E, T> headingPropertyId)
		{
			cols.add(new HeadingToPropertyId(heading,headingPropertyId.getName(),null));
			return this;
			
		}
		public <T extends Object> Builder<E> addGeneratedColumn(String heading, SingularAttribute<E, T> headingPropertyId,ColumnGenerator columnGenerator)
		{
			cols.add(new HeadingToPropertyId(heading,headingPropertyId.getName(),columnGenerator));
			return this;
			
		}

	}
	public List<HeadingToPropertyId> getColumns()
	{
		
		return cols;
	}
}
