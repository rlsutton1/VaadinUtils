package au.com.vaadinutils.fields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

/**
 * A simple class designed to display a PoJo in a table
 * 
 * By default each of the public gettters are displayed in order as a column.
 * 
 * You can pass in a set of visibile columns where the names correspond to the
 * getters name.
 * 
 * @author bsutton
 * 
 */
public class PoJoTable<T> extends Table
{
	private static final long serialVersionUID = 1L;

	Logger logger = Logger.getLogger(PoJoTable.class);

	private ArrayList<Column> columns;

	private String[] visibleColumns;

	public PoJoTable()
	{

	}

	public PoJoTable(String[] visibleColumns)
	{
		this.visibleColumns = visibleColumns;
	}

	public void addRow(T pojo)
	{
		if (columns == null)
		{
			initColumns(pojo);
		}
		Item item = this.getItem(pojo);
		// If the same item is sent again we just update it.
		if (item == null)
			item = this.addItem(pojo);

		for (Column column : columns)
		{
			@SuppressWarnings("unchecked")
			Property<Object> property = item.getItemProperty(column.name);
			property.setValue(column.getValue(pojo));
		}
	}

	private void initColumns(T pojo)
	{
		@SuppressWarnings("unchecked")
		Class<T> objClass = (Class<T>) pojo.getClass();
		columns = new ArrayList<Column>();

		// Get the public methods associated with this class.
		Method[] methods = objClass.getMethods();
		for (Method method : methods)
		{
			String name = method.getName();
			if (name.startsWith("get") || name.startsWith("is"))
			{
				Column column = new Column(method);
				if (this.visibleColumns == null || isVisible(column))
				{
					columns.add(column);
					this.addContainerProperty(column.name, column.type, null);
				}
			}
		}
	}

	private boolean isVisible(Column column)
	{
		boolean isVisible = false;
		for (String name : visibleColumns)
		{
			if (column.getName().equals(name))
				isVisible = true;
		}
		return isVisible;
	}

	class Column
	{
		private String name;
		private Method method;
		private Class<?> type;

		public Column(Method method)
		{
			this.method = method;
			String getterName = method.getName();
			if (getterName.startsWith("get"))
			{
				this.name = getterName.substring(3);
			}
			else
			{
				// must be is
				this.name = getterName.substring(2);
			}
			this.type = method.getReturnType();
		}

		public Object getName()
		{
			return name;
		}

		public Object getValue(T pojo)
		{
			Object ret = null;
			try
			{
				ret = method.invoke(pojo);
			}
			catch (IllegalAccessException e)
			{
				logger.error(e, e);
			}
			catch (InvocationTargetException e)
			{
				logger.error(e, e);
			}
			catch (IllegalArgumentException e)
			{
				logger.error(e, e);
			}

			return ret;
		}

	}
}
