package au.com.vaadinutils.jasper.scheduler;

public interface ReportEmailParameter
{

	String getName();

	String getValue();

	public void setValue(String value,String displayValue);

	String getLabel();

	String getDisplayValue();

}
