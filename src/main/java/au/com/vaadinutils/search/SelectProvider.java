package au.com.vaadinutils.search;

import org.vaadin.viritin.LazyList.CountProvider;
import org.vaadin.viritin.SortableLazyList.SortablePagingProvider;

public interface SelectProvider<T> extends CountProvider, SortablePagingProvider<T>
{

	void setFilterText(String text);

	Class<T> getType();

	String[] getProperties();

	String[] getHeaders();

}
