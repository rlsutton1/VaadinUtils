package au.com.vaadinutils.dao;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;

public class JoinDescriptor<E>
{
	private SingularAttribute<? super E, ?> joinAttribute;

	private JoinType joinType;

	// Logger logger = LogManager.getLogger();
	<J> JoinDescriptor(SingularAttribute<? super E, J> joinAttribute, JoinType joinType)
	{
		this.joinAttribute = joinAttribute;
		this.joinType = joinType;
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
		result = prime * result + ((joinAttribute == null) ? 0 : joinAttribute.hashCode());
		result = prime * result + ((joinType == null) ? 0 : joinType.hashCode());
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
		if (!(obj instanceof JoinDescriptor))
		{
			return false;
		}
		@SuppressWarnings("unchecked")
		JoinDescriptor<E> other = (JoinDescriptor<E>) obj;
		if (joinAttribute == null)
		{
			if (other.joinAttribute != null)
			{
				return false;
			}
		}
		else if (!joinAttribute.equals(other.joinAttribute))
		{
			return false;
		}
		if (joinType != other.joinType)
		{
			return false;
		}
		return true;
	}

}
