package au.com.vaadinutils.jasper.scheduler.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.jasper.scheduler.ScheduledDateParameter;

@Entity
@Table(name = "tblreportemailscheduleddateparameter")
public class ReportEmailScheduledDateParameter implements ScheduledDateParameter,ChildCrudEntity
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	@Enumerated(EnumType.STRING)
	private DateParameterType type;

	@Enumerated(EnumType.STRING)
	private DateParameterOffsetType offsetType;

	private String label;

	private String startName;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	private String endName;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Override
	public DateParameterType getType()
	{
		return type;
	}

	

	public void setType(DateParameterType type)
	{
		this.type = type;

	}

	public void setOffsetType(DateParameterOffsetType offsetType)
	{
		this.offsetType = offsetType;

	}

	@Override
	public DateParameterOffsetType getOffsetType()
	{
		return offsetType;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label2)
	{
		this.label = label2;
		
	}
	
	public void setStartName(String string)
	{
		startName = string;
		
	}


	public void setStartDate(Date value2)
	{
		startDate = value2;
		
	}


	public void setEndName(String string)
	{
		endName= string;
		
	}


	public void setEndDate(Date value2)
	{
		endDate =value2;
		
	}



	@Override
	public Date getStartDate()
	{
		return startDate;
	}



	@Override
	public String getStartName()
	{
		return startName;
	}



	@Override
	public Date getEndDate()
	{
		return endDate;
	}



	@Override
	public String getEndName()
	{
		return endName;
	}



	@Override
	public Long getId()
	{
		return iID;
	}



	@Override
	public void setId(Long id)
	{
		iID = id;
	}



	@Override
	public String getName()
	{
		return ""+iID;
	}



	@Override
	public String getGuid()
	{
		// TODO Auto-generated method stub
		return null;
	}






	

}
