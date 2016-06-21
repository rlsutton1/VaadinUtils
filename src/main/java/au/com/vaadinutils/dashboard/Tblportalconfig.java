package au.com.vaadinutils.dashboard;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import au.com.vaadinutils.entity.BaseCrudEntity;

/**
 * The persistent class for the tblportalconfig database table.
 * 
 */
@Entity
@Table(name = "tblportalconfig")
public class Tblportalconfig extends BaseCrudEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name = "`key`")
	private String key;

	@JoinColumn(name = "portalId")
	private Tblportal portal;

	private String value;

	public void setPortal(Tblportal portal)
	{
		this.portal = portal;
	}

	public Tblportalconfig()
	{
	}

	public String getKey()
	{
		return this.key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

}