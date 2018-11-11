package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

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

	public Date getLastUsed()
	{
		return lastUsed;
	}

	@NotNull
	@Enumerated(EnumType.STRING)
	SaveType saveType;

	long runCount = 0;

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
		Preconditions.checkArgument(StringUtils.isNotBlank(reportSaveparam.getParameterName()));
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

	public void setSaveType(SaveType saveType)
	{
		this.saveType = saveType;
	}

	public void incrementRunCounter()
	{
		runCount++;

	}

	public SaveType getSaveType()
	{
		return saveType;
	}

	public List<ReportSaveParameter> getSortedReportParameters()
	{
		List<ReportSaveParameter> params = new LinkedList<>();
		params.addAll(parameters);
		Collections.sort(params, new Comparator<ReportSaveParameter>()
		{

			@Override
			public int compare(ReportSaveParameter o1, ReportSaveParameter o2)
			{
				return StringUtils.defaultIfBlank(o1.getParameterName(), "")
						.compareTo(StringUtils.defaultIfBlank(o2.getParameterName(), ""));
			}

		});
		return Collections.unmodifiableList(params);

	}

}