package au.com.vaadinutils.validator;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.Logger;

import com.vaadin.data.Validator;

import org.apache.logging.log4j.LogManager;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.ChildCrudEntity;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;

public class UniqueFieldValidator<E extends CrudEntity, F> implements Validator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2263382200263331788L;
	private Class<E> table;
	private SingularAttribute<E, F> matchField;
	final private BaseCrudView<E> crud;
	transient Logger logger = LogManager.getLogger(UniqueFieldValidator.class);
	private String warningMessage;

	/**
	 * for reliable behaviour around new records, the entity should implment
	 * ChildCrudEntity
	 * 
	 * @param matchField
	 * @param crud
	 */
	public UniqueFieldValidator(SingularAttribute<? super E, F> matchField, BaseCrudView<E> crud)
	{
		this(matchField, crud, "'" + matchField.getName() + "' must be unique");
	}

	/**
	 * for reliable behaviour around new records, the entity should implment
	 * ChildCrudEntity
	 * 
	 * @param matchField
	 * @param crud
	 * @param warningMessage
	 */
	@SuppressWarnings("unchecked")
	public UniqueFieldValidator(SingularAttribute<? super E, F> matchField, BaseCrudView<E> crud, String warningMessage)
	{
		this.table = (Class<E>) matchField.getDeclaringType().getJavaType();
		this.matchField = (SingularAttribute<E, F>) matchField;
		this.crud = crud;
		this.warningMessage = warningMessage;

	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (crud != null && crud.getCurrent() != null)
		{
			if (value != null && !("".equals(value.toString())))
			{

				JpaBaseDao<E, Long> dao = new JpaBaseDao<>(table);
				@SuppressWarnings("unchecked")
				List<E> matches = dao.findAllByAttribute(matchField, (F) value, null);
				for (E message : matches)
				{
					Object id = message.getId();
					Object currentId = crud.getCurrent().getId();
					if (message instanceof ChildCrudEntity)
					{
						id = ((ChildCrudEntity) message).getGuid();
						currentId = ((ChildCrudEntity) crud.getCurrent()).getGuid();
					}

					if (!id.equals(currentId))
					{
						logger.error(warningMessage);
						throw new InvalidValueException(warningMessage);
					}
				}
			}
		}

	}

}
