package au.com.vaadinutils.fields;

import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class TextFieldWithButton extends CustomComponent
{
	private TextField textField;
	private Button button;

	public TextFieldWithButton(String caption)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		textField = new TextField();
		textField.setSizeFull();
		layout.addComponent(textField);
		layout.setExpandRatio(textField, 1);
		button = new Button();
		layout.addComponent(button);

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
	
	public void setButtonCaption(String caption)
	{
		button.setCaption(caption);
	}
	
	public void setButtonIcon(Resource icon)
	{
		button.setIcon(icon);
	}

	public String getValue()
	{
		return textField.getValue();
	}
	
	public void addButtonClickListener(ClickListener listener)
	{
		button.addClickListener(listener);
	}
	
	public void setButtonDescription(String description)
	{
		button.setDescription(description);
	}
	
	public void setReadOnly(boolean readOnly)
	{
		textField.setReadOnly(readOnly);
	}
	
	public void setButtonEnabled(boolean enabled)
	{
		button.setEnabled(enabled);
	}
	
}
