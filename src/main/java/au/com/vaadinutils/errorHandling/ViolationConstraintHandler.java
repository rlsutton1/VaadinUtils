package au.com.vaadinutils.errorHandling;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DescriptorException;

import com.vaadin.data.Buffered;
import com.vaadin.data.Buffered.SourceException;

public class ViolationConstraintHandler
{
	static Logger logger = LogManager.getLogger();

	/**
	 * logs the initial error and calls the recusive version of it'self. always
	 * throws a runtime exception
	 * 
	 * @param e
	 */
	static void expandException(Throwable e)
	{
		if (e instanceof RuntimeException && e.getCause() instanceof Buffered.SourceException)
		{
			SourceException ex = (Buffered.SourceException) e.getCause();
			if (ex.getCause() instanceof PersistenceException)
			{
				handlePersistenceException(ex);
			}

		}
		logger.error(e, e);
		handleConstraintViolationException(e, 5);
		throw new RuntimeException(e);
	}

	/**
	 * digs down looking for a useful exception, it will throw a runtime
	 * exception if it finds an useful exception
	 * 
	 * @param e
	 * @param nestLimit
	 */

	private static void handleConstraintViolationException(Throwable e, int nestLimit)
	{
		if (nestLimit > 0 && e != null)
		{
			nestLimit--;
			if (e instanceof DescriptorException)
			{
				DescriptorException desc = (DescriptorException) e;

				throw new RuntimeException(desc.getMessage());
			}
			if (e instanceof ConstraintViolationException)
			{
				String groupedViolationMessage = e.getClass().getSimpleName() + " ";
				for (ConstraintViolation<?> violation : ((ConstraintViolationException) e).getConstraintViolations())
				{
					logger.error(violation.getLeafBean().getClass().getCanonicalName() + " " + violation.getLeafBean());
					String violationMessage = violation.getLeafBean().getClass().getSimpleName() + " "
							+ violation.getPropertyPath() + " " + violation.getMessage() + ", the value was "
							+ violation.getInvalidValue();
					logger.error(violationMessage);
					groupedViolationMessage += violationMessage + "\n";
				}
				throw new RuntimeException(groupedViolationMessage);
			}

			handleConstraintViolationException(e.getCause(), nestLimit);

		}
	}

	static private void handlePersistenceException(Exception e)
	{
		if (e.getCause() instanceof PersistenceException)
		{
			String tmp = e.getMessage();
			PersistenceException pex = (PersistenceException) e.getCause();
			if (pex.getCause() instanceof DatabaseException)
			{
				DatabaseException dex = (DatabaseException) pex.getCause();
				tmp = dex.getMessage();
				if (tmp.indexOf("Query being") > 0)
				{
					// strip of the query
					tmp = tmp.substring(0, tmp.indexOf("Query being"));

					if (tmp.contains("MySQL"))
					{
						tmp = tmp.substring(tmp.indexOf("MySQL") + 5);
					}
				}
			}
			logger.error(e, e);
			throw new RuntimeException(tmp);
		}
	}
}
