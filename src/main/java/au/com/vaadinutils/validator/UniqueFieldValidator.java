package au.com.vaadinutils.validator;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	transient Logger logger = LogManager.getLogger(UniqueFieldValidator.class);
	private String warningMessage;

	public UniqueFieldValidator(SingularAttribute<E, F> matchField, BaseCrudView<E> crud)
	{
		this(matchField, crud, "'" + matchField.getName() + "' must be unique");
	}

	public UniqueFieldValidator(SingularAttribute<E, F> matchField, BaseCrudView<E> crud, String warningMessage)
	{
		this.table = matchField.getDeclaringType().getJavaType();
		this.matchField = matchField;
		this.crud = crud;
		this.warningMessage = warningMessage;

	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null && !("".equals(value.toString())))
		{

			JpaBaseDao<E, Long> dao = new JpaBaseDao<E, Long>(table);
			@SuppressWarnings("unchecked")
			List<E> matches = dao.findAllByAttribute(matchField, (F) value, null);
			for (E message : matches)
			{
				if (crud != null && crud.getCurrent() != null && !message.getId().equals(crud.getCurrent().getId()))
				{
					logger.error(warningMessage);
					throw new InvalidValueException(warningMessage);
				}
				if (crud == null)
				{
					logger.error(warningMessage);
					throw new InvalidValueException(warningMessage);
				}
			}
		}

	}

}
