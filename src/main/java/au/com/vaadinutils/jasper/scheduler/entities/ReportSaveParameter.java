package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * The persistent class for the tblcampaignallocation database table.
 * 
 */
@Entity
@Table(name = "tblreportsaveparameter")
public class ReportSaveParameter implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(updatable = false)
	String guid = UUID.randomUUID().toString();

	@JoinColumn(name = "reportSaveId")
	ReportSave parent;

	String parameterName;

	String parameterValue;

	String textualRepresentation;

	public String getParameterName()
	{
		return parameterName;
	}

	public String getTextualRepresentation()
	{
		return textualRepresentation;
	}

	public void setParameterName(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public void setTextualRepresentation(String textualRepresentation)
	{
		this.textualRepresentation = textualRepresentation;
	}

	public void setParameterValue(String parameterValue)
	{
		this.parameterValue = parameterValue;
	}

	public void setParent(ReportSave parent)
	{
		this.parent = parent;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportSaveParameter other = (ReportSaveParameter) obj;
		if (guid == null)
		{
			if (other.guid != null)
				return false;
		}
		else if (!guid.equals(other.guid))
			return false;
		return true;
	}

	public String getParameterValue()
	{
		return parameterValue;
	}

	public Long getId()
	{
		return id;
	}
}