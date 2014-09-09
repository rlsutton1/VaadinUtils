package au.com.vaadinutils.validator;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudEntity;

import com.vaadin.data.Validator;

public class UniqueFieldValidatorWithContainerFilters<E extends CrudEntity, F> implements Validator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2263382200263331788L;
	private SingularAttribute<E, F> matchField;
	private BaseCrudView<E> crud;
	transient Logger logger = LogManager.getLogger(UniqueFieldValidatorWithContainerFilters.class);

	/**
	 * only validates uniqueness against records that match the current
	 * containers filters.
	 * 
	 * @param matchField
	 * @param crud
	 */
	public UniqueFieldValidatorWithContainerFilters(SingularAttribute<E, F> matchField, BaseCrudView<E> crud)
	{
		this.matchField = matchField;
		this.crud = crud;

	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null && crud.getCurrent()!=null)
		{
			for (Object id : crud.getContainer().getItemIds())
			{
				if (!id.equals(crud.getCurrent().getId()))
				{
					Object existingValue = crud.getContainer().getItem(id).getItemProperty(matchField.getName())
							.getValue();

					if (existingValue != null && existingValue.equals(value))
					{
						String message2 = "'" + matchField.getName() + "' must be unique";
						logger.error(message2);
						throw new InvalidValueException(message2);
					}
				}
			}
		}

	}

}
