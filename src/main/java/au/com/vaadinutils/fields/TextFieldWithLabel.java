package au.com.vaadinutils.fields;

import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class TextFieldWithLabel extends CustomComponent
{
	protected TextField textField;
	private Label label;

	public TextFieldWithLabel(String caption)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		textField = new TextField();
		textField.setSizeFull();
		layout.addComponent(textField);
		label = new Label();
		label.setSizeFull();
		layout.addComponent(label);

		setCompositionRoot(layout);
		setCaption(caption);
	}

	public void setPropertyDataSource(EntityItemProperty newDataSource)
	{
		textField.setPropertyDataSource(newDataSource);
	}

	public void setNullRepresentation(String nullRepresentation)
	{
		textField.setNullRepresentation(nullRepresentation);
	}

	public void setLabelCaption(String caption)
	{
		label.setCaption(caption);
	}

	public void setLabelIcon(Resource icon)
	{
		label.setIcon(icon);
	}

	public void setValue(final String newValue)
	{
		textField.setValue(newValue);
	}

	public String getValue()
	{
		return textField.getValue();
	}

	public void setButtonDescription(String description)
	{
		label.setDescription(description);
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		textField.setReadOnly(readOnly);
	}

	public void setLabelReadOnly(boolean readOnly)
	{
		label.setReadOnly(readOnly);
	}

	public void addValueChangeListener(ValueChangeListener listener)
	{
		textField.addValueChangeListener(listener);
	}

	public void setConverter(Class<?> datamodelType)
	{
		textField.setConverter(datamodelType);
	}

	public void setConverter(Converter<String, ?> converter)
	{
		textField.setConverter(converter);
	}

	public TextField getTextField()
	{
		return textField;
	}
}
