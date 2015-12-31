package au.com.vaadinutils.validator;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.JpaBaseDao.Condition;
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
	private FilterValueCallback<FILTER_FIELD_TYPE> filterCallback;

	public UniqueFieldValidatorJPA(SingularAttribute<E, UNIQUE_FIELD_TYPE> matchField, String warningMessage,
			SingularAttribute<E, FILTER_FIELD_TYPE> filterAttribute, FilterValueCallback<FILTER_FIELD_TYPE> callback)
	{
		this.table = matchField.getDeclaringType().getJavaType();
		this.matchField = matchField;

		this.warningMessage = warningMessage;
		this.filterAttribute = filterAttribute;
		this.filterCallback =callback;

	}

	public UniqueFieldValidatorJPA(SingularAttribute<E, UNIQUE_FIELD_TYPE> matchField, String warningMessage,
			SingularAttribute<E, FILTER_FIELD_TYPE> filterAttribute,final FILTER_FIELD_TYPE filterValue)
	{
		this.table = matchField.getDeclaringType().getJavaType();
		this.matchField = matchField;

		this.warningMessage = warningMessage;
		this.filterAttribute = filterAttribute;
		this.filterCallback =new FilterValueCallback<FILTER_FIELD_TYPE>()
		{

			@Override
			public FILTER_FIELD_TYPE getValue()
			{
				return filterValue;
			}
		};

	}

	

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null && !("".equals(value.toString())))
		{

			JpaDslBuilder<E> q = new JpaBaseDao<E, Long>(table).find();

			Condition<E> criteria = q.eq(matchField, (UNIQUE_FIELD_TYPE) value);
			if (filterAttribute != null && filterCallback.getValue() != null)
			{
				criteria = criteria.and(q.eq(filterAttribute, filterCallback.getValue()));
			}
			Long count = q.where(criteria).count();
			if (count > 0)
			{
				throw new InvalidValueException(warningMessage);
			}

		}

	}

}
