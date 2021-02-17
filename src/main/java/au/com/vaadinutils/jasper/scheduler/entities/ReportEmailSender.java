package au.com.vaadinutils.jasper.scheduler.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import au.com.vaadinutils.crud.CrudEntity;

@Entity
@Table(name = "tblreportemailsender")
public class ReportEmailSender implements CrudEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5079828648797583451L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	public String username;

	boolean isAdmin;

	public void setUserName(String username2)
	{
		username = username2;

	}

	@Override
	public String toString()
	{
		return username;
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
		return username;
	}
}
