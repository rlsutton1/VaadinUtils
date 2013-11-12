package au.com.vaadinutils.validator;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;

import com.vaadin.data.Validator;

public class UniqueFieldValidator<E extends CrudEntity, F> implements Validator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2263382200263331788L;
	private Class<E> table;
	private SingularAttribute<E, F> matchField;
	private BaseCrudView<E> crud;

	public UniqueFieldValidator(Class<E> table, SingularAttribute<E, F> matchField, BaseCrudView<E> crud)
	{
		this.table = table;
		this.matchField = matchField;
		this.crud = crud;

	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		JpaBaseDao<E, Long> dao = new JpaBaseDao<E, Long>(table);
		@SuppressWarnings("unchecked")
		List<E> matches = dao.findAllByAttribute(matchField, (F) value, null);
		for (E message : matches)
		{
			if (message.getId() != crud.getCurrent().getId())
			{
				throw new InvalidValueException("'" + matchField.getName() + "' must be unique");
			}
		}

	}

}
