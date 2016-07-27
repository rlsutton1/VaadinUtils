package au.com.vaadinutils.dashboard;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;

import au.com.vaadinutils.entity.BaseCrudEntity;

/**
 * The persistent class for the tblportalconfig database table.
 * 
 */
@Entity
@Table(name = "tblportaldata")
public class TblPortalData extends BaseCrudEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Lob
	@Column(length = 100000)
	private byte[] data;

	@JoinColumn(name = "portalId")
	private Tblportal portal;

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] byteArray)
	{
		data = byteArray;

	}

	public void setPortal(Tblportal portal2)
	{
		portal = portal2;
		portal.data = this;

	}

}