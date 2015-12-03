package au.com.vaadinutils.jasper.scheduler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

public class DayOfWeekCheckBoxes extends HorizontalLayout implements Field<String>
{
	// Logger logger = LogManager.getLogger();

	private static final long serialVersionUID = -3339061540299550077L;

	private Property<String> datasource;

	List<CheckBox> boxes = new LinkedList<CheckBox>();

	private boolean required;

	/**
	 * days of week as per DateTimeConstants mon = 1 , sun = 7
	 */

	public DayOfWeekCheckBoxes()
	{
		setSpacing(true);
		setSizeFull();
		String days1[] = new String[] { "Mon", "Tue", "Wed", "Thur", "Fri", "Sat", "Sun" };
		int ctr = 1;
		for (String day : days1)
		{
			CheckBox dayCheck = new CheckBox(day);
			dayCheck.setData(ctr++);

			addComponent(dayCheck);
			boxes.add(dayCheck);
		}
	}

	public void focus()
	{
		super.focus();
	}

	@Override
	public boolean isInvalidCommitted()
	{
		return false;
	}

	@Override
	public void setInvalidCommitted(boolean isCommitted)
	{

	}

	@Override
	public void commit() throws SourceException, InvalidValueException
	{
		datasource.setValue(getValue());

	}

	@Override
	public void discard() throws SourceException
	{
		setValue(datasource.getValue());

	}

	@Override
	public void setBuffered(boolean buffered)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBuffered()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isModified()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addValidator(Validator validator)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeValidator(Validator validator)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllValidators()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Validator> getValidators()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void validate() throws InvalidValueException
	{
		if (isVisible() && getValue().length() == 0)
		{
			throw new InvalidValueException("You must select at least one day");
		}

	}

	@Override
	public boolean isInvalidAllowed()
	{
		return false;
	}

	@Override
	public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException
	{

	}

	@Override
	public String getValue()
	{
		String value = "";
		int i = 1;
		for (CheckBox box : boxes)
		{
			if (box.getValue())
			{
				if (value.length() > 0)
				{
					value += ",";
				}
				value += i;
			}
			i++;
		}
		return value;
	}

	@Override
	public void setValue(String newValue) throws com.vaadin.data.Property.ReadOnlyException
	{
		for (CheckBox box : boxes)
		{
			box.setValue(false);
		}
		if (newValue != null && newValue.length() > 0)
		{
			String[] values = newValue.split(",");
			for (String value : values)
			{
				int index = Integer.parseInt(value);
				if (index > 0)
				{
					boxes.get(index - 1).setValue(true);
				}
			}
		}

	}

	@Override
	public Class<? extends String> getType()
	{

		return String.class;
	}

	@Override
	public void addValueChangeListener(com.vaadin.data.Property.ValueChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(com.vaadin.data.Property.ValueChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeValueChangeListener(com.vaadin.data.Property.ValueChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(com.vaadin.data.Property.ValueChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPropertyDataSource(@SuppressWarnings("rawtypes") Property newDataSource)
	{
		datasource = newDataSource;
		if (datasource != null)
		{
			setValue(datasource.getValue());
		}
		else
		{
			setValue(null);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Property getPropertyDataSource()
	{
		return datasource;
	}

	@Override
	public int getTabIndex()
	{
		return 0;
	}

	@Override
	public void setTabIndex(int tabIndex)
	{

	}

	@Override
	public boolean isRequired()
	{
		return required;
	}

	@Override
	public void setRequired(boolean required)
	{
		this.required = required;

	}

	@Override
	public void setRequiredError(String requiredMessage)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getRequiredError()
	{
		return "You must select at least one day";
	}

	@Override
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub

	}

}
