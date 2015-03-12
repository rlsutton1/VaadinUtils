package au.com.vaadinutils.ui;

import com.zybnet.autocomplete.server.AutocompleteField;
import com.zybnet.autocomplete.shared.AutocompleteServerRpc;

public class AutoCompleteTextField<E> extends AutocompleteField<E> implements AutocompleteServerRpc
{

    private static final long serialVersionUID = -7921633418513324024L;
    String originalValue;
    private String currentValue;

    @Override
    protected void setInternalValue(String newValue)
    {

	super.setInternalValue(newValue);
	setValue(newValue);
    }

    /**
     * protect against possible recursion/stack overflow
     */
    boolean inSetValue;

    @Override
    public void setValue(String newValue) throws ReadOnlyException
    {
	if (!inSetValue)
	{
	    try
	    {
		inSetValue = true;
		super.setValue(newValue);
		setText(newValue);
		originalValue = newValue;
		currentValue = newValue;
	    }
	    finally
	    {
		inSetValue = false;
	    }
	}

    }

    @Override
    public String getValue()
    {
	return currentValue;
    }

    @Override
    public Object getConvertedValue()
    {
	return currentValue;
    }

    @Override
    protected String getInternalValue()
    {
	return currentValue;
    }

    public void onQuery(String query)
    {
	super.onQuery(query);
	currentValue = query;
    }

    @Override
    public boolean isModified()
    {
	boolean ret = super.isModified();
	ret |= originalValue == null && currentValue != null;
	if (originalValue != null)
	{
	    ret |= !originalValue.equals(currentValue);
	}
	if (!ret)
	{
	    System.out.println("NOt dirty");
	}
	return ret;
    }
}
