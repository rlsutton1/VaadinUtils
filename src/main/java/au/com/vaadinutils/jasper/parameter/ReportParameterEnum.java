package au.com.vaadinutils.jasper.parameter;

import au.com.vaadinutils.crud.FormHelper;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class ReportParameterEnum<T extends Enum<T>> extends ReportParameter<Enum<T>>
{

	private ComboBox field;

	/**
	 * 
	 * @param caption
	 * @param defaultValue
	 * @param parameterName
	 * @param enumClass
	 */
	public ReportParameterEnum(String caption, T defaultValue,String parameterName, Class<T> enumClass)
	{
		super(parameterName);
		field = new ComboBox(caption);
		field.setContainerDataSource(FormHelper.createContainerFromEnumClass("value", enumClass));
		field.setNewItemsAllowed(false);
		field.setNullSelectionAllowed(false);
		field.setTextInputAllowed(false);
		field.setValue(defaultValue);
	}

	@Override
	public String getValue()
	{
		return field.getValue().toString();
	}

	@Override
	public Component getComponent()
	{
		return field;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
	}

	@Override
	public void setDefaultValue(Enum<T> defaultValue)
	{
		field.setValue(defaultValue);
		
	}

	@Override
	public String getExpectedParameterClassName()
	{
		return String.class.getCanonicalName();
	}
}
