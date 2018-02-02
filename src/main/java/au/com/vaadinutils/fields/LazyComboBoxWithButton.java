package au.com.vaadinutils.fields;

import java.util.Collection;

import org.vaadin.viritin.LazyList;
import org.vaadin.viritin.fields.LazyComboBox;
import org.vaadin.viritin.fields.LazyComboBox.FilterableCountProvider;
import org.vaadin.viritin.fields.LazyComboBox.FilterablePagingProvider;
import org.vaadin.viritin.fields.MValueChangeListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class LazyComboBoxWithButton<E> extends CustomComponent
{
	private Class<E> itemClass;
	private FilterablePagingProvider<E> pagingProvider;
	private FilterableCountProvider countProvider;
	private int pageSize = LazyList.DEFAULT_PAGE_SIZE;
	private Button button;
	private LazyComboBox<E> field;

	public LazyComboBoxWithButton(final Class<E> itemClass, final FilterablePagingProvider<E> pagingProvider,
			final FilterableCountProvider countProvider, final int pageSize, final String caption, final Button button)
	{
		this.itemClass = itemClass;
		this.pagingProvider = pagingProvider;
		this.countProvider = countProvider;
		this.pageSize = pageSize;

		if (button != null)
		{
			this.button = button;
		}
		else
		{
			this.button = new Button();
		}

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		field = createField();
		field.setSizeFull();
		layout.addComponent(field);
		layout.setExpandRatio(field, 1);
		layout.addComponent(this.button);

		setCompositionRoot(layout);
		setCaption(caption);
	}

	private LazyComboBox<E> createField()
	{
		return new LazyComboBox<>(itemClass, pagingProvider, countProvider, pageSize);
	}

	public LazyComboBox<E> getField()
	{
		return field;
	}

	public Button getButton()
	{
		return button;
	}

	public E getValue()
	{
		return field.getValue();
	}

	public void setValue(final E newFieldValue)
	{
		field.setValue(newFieldValue);
	}

	public void addMValueChangeListener(final MValueChangeListener<E> listener)
	{
		field.addMValueChangeListener(listener);
	}

	public void removeMValueChangeListener(final MValueChangeListener<E> listener)
	{
		field.removeMValueChangeListener(listener);
	}

	@Override
	public Collection<?> getListeners(Class<?> eventType)
	{
		return field.getListeners(eventType);
	}
}