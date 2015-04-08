package au.com.vaadinutils.dao;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

public interface JoinMetaData<E, K>
{

	Join<E, K> getJoin(Root<E> root);

	Join<E, K> getJoin(Join<?, E> join);

}
