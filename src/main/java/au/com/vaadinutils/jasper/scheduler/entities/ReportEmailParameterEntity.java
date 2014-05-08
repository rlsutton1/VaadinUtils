package au.com.vaadinutils.jasper.scheduler.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import au.com.vaadinutils.jasper.scheduler.ReportEmailParameter;

@Entity
@Table(name = "tblReportEmailParameterEntity")

public class ReportEmailParameterEntity implements ReportEmailParameter
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	private String name;
	
	@Column(length=8192)
	private String value;

	private String label;

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

	public void setValue(String value)
	{
		this.value = value;
		
	}


	public void setLabel(String label)
	{
		this.label =label;
		
	}



}
