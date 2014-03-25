package au.com.vaadinutils.fields;

import java.util.ArrayList;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.ui.Field;

public class FieldValidator
{
	private ArrayList<Field<?>> fields = new ArrayList<Field<?>>();

	public void validate()
	{
		for (Field<?> field : fields)
		{
			try
			{
				field.validate();
			}
			catch (EmptyValueException e)
			{
				throw new Validator.InvalidValueException(field.getCaption() + " may not be empty.");
			}
		}
	}

	public void addField(Field<?> field)
	{
		fields.add(field);
	}

}
