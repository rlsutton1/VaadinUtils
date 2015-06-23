package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.errorHandling.ErrorWindow;

public class Transaction
{
	private EntityTransaction transaction;
	private boolean nested = false;
	Logger logger = LogManager.getLogger();

	public Transaction(EntityManager em)
	{
		transaction = em.getTransaction();

		// Only begin if we are not already in a transaction.
		// Eclipselink doesn't support nested transactions
		if (!transaction.isActive())
			transaction.begin();
		else
		{

			logger.warn("Nested transaction!!!!");
			nested = true;
		}
	}

	public void close()
	{
		if (transaction.isActive() && !nested)
			rollback();
	}

	private void rollback()
	{
		transaction.rollback();
	}

	public void commit()
	{
		try{
		if (!nested)
		{
			if (transaction.isActive())
				transaction.commit();
			else
				throw new IllegalStateException("Commit has already been called on the transaction");
		}
		}catch (Exception e)
		{
			ErrorWindow.showErrorWindow(e);
			throw e;
		}

	}

	/*
	 * Begins a transaction. You don't normally need to call this as the ctor
	 * automatically calls begin. If however you want to re-use the transaction
	 * in a try/finally block you can call commit and then begin again.
	 */
	public void begin()
	{
		if (transaction.isActive())
			throw new IllegalStateException("Begin has already been called on the transaction");

		transaction.begin();
	}

}
