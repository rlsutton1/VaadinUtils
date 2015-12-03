package au.com.vaadinutils.validator;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.JpaDslBuilder;

import com.vaadin.data.Validator;

public class UniqueFieldValidatorJPA<E extends CrudEntity, UNIQUE_FIELD_TYPE, FILTER_FIELD_TYPE> implements Validator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2263382200263331788L;
	private Class<E> table;
	private SingularAttribute<E, UNIQUE_FIELD_TYPE> matchField;
	transient Logger logger = LogManager.getLogger(UniqueFieldValidatorJPA.class);
	private String warningMessage;
	private SingularAttribute<E, FILTER_FIELD_TYPE> filterAttribute;
	private FILTER_FIELD_TYPE filterValue;

	public UniqueFieldValidatorJPA(SingularAttribute<E, UNIQUE_FIELD_TYPE> matchField, String warningMessage,
			SingularAttribute<E, FILTER_FIELD_TYPE> filterAttribute, FILTER_FIELD_TYPE filterValue)
	{
		this.table = matchField.getDeclaringType().getJavaType();
		this.matchField = matchField;

		this.warningMessage = warningMessage;
		this.filterAttribute = filterAttribute;
		this.filterValue = filterValue;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null && !("".equals(value.toString())))
		{

			JpaDslBuilder<E> q = new JpaBaseDao<E, Long>(table).find();

			Long count = q.where(q.eq(filterAttribute, filterValue).and(q.eq(matchField, (UNIQUE_FIELD_TYPE) value)))
					.count();
			if (count > 0)
			{
				throw new InvalidValueException(warningMessage);
			}

		}

	}

}
