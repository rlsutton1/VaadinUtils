package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the tblcampaignallocation database table.
 * 
 */
@Entity
@Table(name = "tblreportsave")
public class ReportSave implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "`user`")
	String user = "";

	String userDescription;

	@Temporal(TemporalType.TIMESTAMP)
	Date lastUsed = new Date();

	String reportClass;

	@OneToMany(mappedBy = "parent")
	Set<ReportSaveParameter> parameters = new HashSet<>();

	public String getUserDescription()
	{
		return userDescription;
	}

	public Set<ReportSaveParameter> getParameters()
	{
		return parameters;
	}

	public String getReportClass()
	{
		return reportClass;
	}

	public void setReportClass(String reportClass)
	{
		this.reportClass = reportClass;
	}

	public void setUserDescription(String userDescription)
	{
		this.userDescription = userDescription;
	}

	public void addParameter(ReportSaveParameter reportSaveparam)
	{
		parameters.add(reportSaveparam);
		reportSaveparam.setParent(this);

	}

	public Long getId()
	{
		return id;
	}

	public void setUser(String username)
	{
		this.user = username;

	}

}