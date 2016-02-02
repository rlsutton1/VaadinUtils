package au.com.vaadinutils.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.dao.JpaEntityHelper;


@MappedSuperclass
public abstract class BaseCrudEntity implements ChildCrudEntity
{
	// Logger logger = LogManager.getLogger();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(updatable = false)
	String guid = JpaEntityHelper.getGuid();

	public String getGuid()
	{
		return guid;
	}

	@Override
	public Long getId()
	{
		return id;
	}

	@Override
	public void setId(Long id)
	{
		this.id = id;

	}

	@Override
	public String getName()
	{
		return this.getClass().getSimpleName() + " " + guid + " " + id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof BaseCrudEntity))
		{
			return false;
		}
		BaseCrudEntity other = (BaseCrudEntity) obj;
		if (guid == null)
		{
			if (other.guid != null)
			{
				return false;
			}
		}
		else if (!guid.equals(other.guid))
		{
			return false;
		}
		return true;
	}
}
