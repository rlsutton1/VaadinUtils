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

import au.com.vaadinutils.jasper.scheduler.DateParameterType;
import au.com.vaadinutils.jasper.scheduler.ScheduledDateParameter;

@Entity
@Table(name = "tblReportEmailScheduledDateParameter")
public class ReportEmailScheduledDateParameter implements ScheduledDateParameter
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	@Enumerated(EnumType.STRING)
	private DateParameterType type;

	@Enumerated(EnumType.STRING)
	private DateParameterOffsetType offsetType;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	private String name;

	private String label;

	@Override
	public DateParameterType getType()
	{
		return type;
	}

	@Override
	public Date getDate()
	{
		return date;
	}

	// Logger logger = LogManager.getLogger();

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String parameterName)
	{
		name = parameterName;

	}

	public void setDate(Date date)
	{
		this.date = date;

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
}
