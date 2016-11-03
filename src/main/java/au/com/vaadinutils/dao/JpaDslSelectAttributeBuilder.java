package au.com.vaadinutils.dao;

import javax.persistence.metamodel.SingularAttribute;

public class JpaDslSelectAttributeBuilder<E, R> extends JpaDslAbstract<E, R>
{

	public JpaDslSelectAttributeBuilder(Class<E> entityClass, SingularAttribute<E, R> resultAttribute)
	{
		this.entityClass = entityClass;
		builder = getEntityManager().getCriteriaBuilder();

		criteria = builder.createQuery(resultAttribute.getBindableJavaType());
		root = criteria.from(entityClass);

		criteria.select(root.get(resultAttribute));

	}

}
