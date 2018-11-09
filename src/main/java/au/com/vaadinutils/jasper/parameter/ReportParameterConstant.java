package au.com.vaadinutils.jasper.parameter;

import java.lang.reflect.ParameterizedType;

import com.vaadin.ui.Component;

import au.com.vaadinutils.jasper.scheduler.entities.DateParameterType;

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
	 * use this constructor if you want this parameter to show in the
	 * "Parameters" section of the report.
	 * 
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
	public String getLabel(String parameterName)
	{
		return displayLabel;
	}

	@Override
	public boolean displayInreport()
	{
		return displayInReport;
	}

	@Override
	public T getValue(String parameterName)
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
		return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0])
				.getCanonicalName();
	}

	@Override
	public boolean showFilter()
	{
		return false;
	}

	@Override
	public String getDisplayValue(String parameterName)
	{
		return displayValue;
	}

	@Override
	public boolean validate()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValueAsString(String value, String parameterName)
	{
		this.value = (T) value;

	}

	@Override
	public boolean isDateField()
	{
		return false;
	}

	@Override
	public DateParameterType getDateParameterType()
	{
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String getSaveMetaData()
	{
		return "";
	}

	@Override
	public void applySaveMetaData(String metaData)
	{
		// NO OP
	}

	@Override
	public String getMetaDataComment()
	{
		return "";
	}

}
