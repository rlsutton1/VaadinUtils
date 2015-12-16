package au.com.vaadinutils.crud.columnGenerators;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

public class CheckboxColumnGenerator implements ColumnGenerator
{
	private static final long serialVersionUID = 1L;

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId)
	{
		// Need a layout to set center alignment on the checkbox.
		// http://dev.vaadin.com/ticket/12027
		final VerticalLayout layout = new VerticalLayout();
		final CheckBox checkbox = new CheckBox();
		layout.addComponent(checkbox);
		layout.setComponentAlignment(checkbox, Alignment.MIDDLE_CENTER);
		@SuppressWarnings("unchecked")
		final Property<Boolean> property = source.getItem(itemId).getItemProperty(columnId);
		checkbox.setValue(property.getValue());

		checkbox.addValueChangeListener(new ValueChangeListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				property.setValue((Boolean) event.getProperty().getValue());
			}
		});

		((ValueChangeNotifier) property).addValueChangeListener(new ValueChangeListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event)
			{
				checkbox.setValue((Boolean) event.getProperty().getValue());
			}
		});

		return layout;
	}
}
