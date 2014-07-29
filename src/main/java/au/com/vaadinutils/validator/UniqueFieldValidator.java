package au.com.vaadinutils.validator;

import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.BaseCrudView;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;

import com.vaadin.addon.jpacontainer.EntityItemProperty;
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

	public UniqueFieldValidator(SingularAttribute<E, F> matchField, BaseCrudView<E> crud)
	{
		this.table = matchField.getDeclaringType().getJavaType();
		this.matchField = matchField;
		this.crud = crud;

	}

	@Override
	public void validate(Object value) throws InvalidValueException
	{
		if (value != null)
		{
			for (Object id: crud.getContainer().getItemIds())
			{
				System.out.println(id+" " +crud.getContainer().getItem(id).getItemProperty(matchField.getName()).getValue());
				if (!id.equals(crud.getCurrent().getId()))
				{
					Object existingValue = crud.getContainer().getItem(id).getItemProperty(matchField.getName()).getValue();
					if (existingValue.equals(value))
					{
						String message2 = "'" + matchField.getName() + "' must be unique";
						logger.error(message2);
						throw new InvalidValueException(message2);
					}
				}
			}
//			JpaBaseDao<E, Long> dao = new JpaBaseDao<E, Long>(table);
//			@SuppressWarnings("unchecked")
//			List<E> matches = dao.findAllByAttribute(matchField, (F) value, null);
//			for (E message : matches)
//			{
//				if (crud == null || !message.getId().equals(crud.getCurrent().getId()))
//				{
//
//					String message2 = "'" + matchField.getName() + "' must be unique";
//					logger.error(message2);
//					throw new InvalidValueException(message2);
//				}
//			}
		}

	}

}
