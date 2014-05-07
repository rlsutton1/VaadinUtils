package au.com.vaadinutils.jasper.scheduler.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "tblReportEmailTimeOfDayToRun")
public class ReportEmailTimeOfDayToRun
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	@Temporal(TemporalType.TIME)
	private Date timeToRun;

	public Date getTime()
	{
		return timeToRun;
	}

}
