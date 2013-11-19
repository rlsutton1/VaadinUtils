package au.com.vaadinutils.crud.splitFields;

import com.vaadin.shared.ui.combobox.FilteringMode;

public class SplitFilteredComboBox extends SplitComboBox
{

	private static final long serialVersionUID = 5244767300170706260L;
	private Filter filter;

	public SplitFilteredComboBox(String label, Filter filter)
	{
		super(label);
		this.filter = filter;
	}

	protected Filter buildFilter(String filterString, FilteringMode filteringMode)
	{

		return filter;

	}
}
