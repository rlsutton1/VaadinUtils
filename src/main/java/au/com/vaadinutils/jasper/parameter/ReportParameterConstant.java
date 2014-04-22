package au.com.vaadinutils.jasper.parameter;

import java.lang.reflect.ParameterizedType;

import com.vaadin.ui.Component;

public class ReportParameterConstant<T> extends ReportParameter<T>
{

	private T value;
	private String displayValue;
	private boolean displayInReport;
	private String displayLabel;

	/**
	 * calling this constructor will mean that this report parameter will not be
	 * visiable in the "Parameters" section of the report
	 * 
	 * @param parameterName
	 * @param value
	 */
	public ReportParameterConstant(String parameterName, T value)
	{
		super(parameterName, parameterName);
		this.value = value;
		this.displayInReport = false;
	}

	/**
	 * use this constructor if you want this parameter to show in the "Parameters" section of the report. 
	 * @param parameterName
	 * @param value
	 * @param displayLabel
	 * @param displayValue
	 */
	public ReportParameterConstant(String parameterName, T value, String displayLabel, String displayValue)
	{
		super(parameterName, parameterName);
		this.value = value;
		this.displayValue = displayValue;
		this.displayInReport = true;
		this.displayLabel = displayLabel;
	}

	@Override
	public String getLabel()
	{
		return displayLabel;
	}
	
	@Override
	public boolean displayInreport()
	{
		return displayInReport;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public Component getComponent()
	{
		return null;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
	}

	@Override
	public void setDefaultValue(T defaultValue)
	{
		// NOOP

	}

	@SuppressWarnings("unchecked")
	@Override
	public String getExpectedParameterClassName()
	{
		return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getCanonicalName();
	}

	public boolean showFilter()
	{
		return false;
	}

	@Override
	public String getDisplayValue()
	{
		return displayValue;
	}

	@Override
	public boolean validate()
	{
		return true;
	}
}
