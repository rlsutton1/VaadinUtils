package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import au.com.vaadinutils.jasper.scheduler.ReportEmailParameter;

@Entity
@Table(name = "tblreportemailparameterentity")

public class ReportEmailParameterEntity implements ReportEmailParameter, Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	private String name;

	@Column(length = 8192)
	private String value;

	private String label;

	private String displayValue;

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	public void setName(String parameterName)
	{
		name = parameterName;

	}

	@Override
	public void setValue(String value, String displayValue)
	{
		this.value = value;
		this.displayValue = displayValue;

	}

	public void setLabel(String label)
	{
		this.label = label;

	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public String getDisplayValue()
	{
		return displayValue;
	}

}
