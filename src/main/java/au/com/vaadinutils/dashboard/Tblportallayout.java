package au.com.vaadinutils.dashboard;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import au.com.vaadinutils.entity.BaseCrudEntity;

/**
 * The persistent class for the tblportallayout database table.
 * 
 */
@Entity
@Table(name = "tblportallayout")
public class Tblportallayout extends BaseCrudEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name = "agentId")
	private Long account;

	@Column(name = "`default`")
	private Boolean default_ = false;

	private String name;

	public Tblportallayout()
	{
	}

	public Long getAccount()
	{
		return account;
	}

	public void setAccount(Long account)
	{
		this.account = account;
	}

	@OneToMany(mappedBy = "portalLayout")
	private Set<Tblportal> portals = new HashSet<>();

	public Boolean getDefault_()
	{
		return this.default_;
	}

	public void setDefault_(Boolean default_)
	{
		this.default_ = default_;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Set<Tblportal> getPortals()
	{
		return portals;

	}

	public void addPortal(Tblportal portal)
	{
		portal.setPortalLayout(this);
		portals.add(portal);

	}

	public void removePortal(Tblportal portal)
	{

		portals.remove(portal);

	}

	@Override
	public String toString()
	{
		if (default_)
		{
			return "* " + name;
		}
		return name;
	}

}