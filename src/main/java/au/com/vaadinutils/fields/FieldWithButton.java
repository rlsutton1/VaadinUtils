package au.com.vaadinutils.fields;

import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public abstract class FieldWithButton<T> extends CustomComponent
{
	protected AbstractField<T> field;
	private Button button;

	public FieldWithButton(final String caption)
	{
		this(caption, null);
	}

	public FieldWithButton(final String caption, final Button button)
	{
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
		layout.addComponent(button);

		setCompositionRoot(layout);
		setCaption(caption);
	}

	protected abstract AbstractField<T> createField();

	public void setPropertyDataSource(EntityItemProperty newDataSource)
	{
		field.setPropertyDataSource(newDataSource);
	}

	public void setButtonCaption(String caption)
	{
		button.setCaption(caption);
	}

	public void setButtonIcon(Resource icon)
	{
		button.setIcon(icon);
	}

	public T getValue()
	{
		return field.getValue();
	}

	public void addButtonClickListener(ClickListener listener)
	{
		button.addClickListener(listener);
	}

	public void addTextFieldValueChangeListener(ValueChangeListener listener)
	{
		field.addValueChangeListener(listener);
	}

	public void setButtonDescription(String description)
	{
		button.setDescription(description);
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		field.setReadOnly(readOnly);
	}

	public void setButtonEnabled(boolean enabled)
	{
		button.setEnabled(enabled);
	}

	public void setValue(final T newValue)
	{
		field.setValue(newValue);
	}

	public Button getButton()
	{
		return button;
	}
}
