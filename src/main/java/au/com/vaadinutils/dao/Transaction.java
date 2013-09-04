package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class Transaction implements AutoCloseable
{
	private EntityTransaction transaction;

	public Transaction(EntityManager em)
	{
		transaction = em.getTransaction();
		transaction.begin();
	}

	@Override
	public void close()
	{
		if (transaction.isActive())
			rollback();
	}

	private void rollback()
	{
		transaction.rollback();
	}

	public void commit()
	{
		transaction.commit();

	}

	/*
	 * Begins a transaction. You don't normally need to call this as the ctor
	 * automatically calls begin. If however you want to re-use the transaction
	 * in a try/finally block you can call commit and then begin again.
	 */
	public void begin()
	{
		if (!transaction.isActive())
			throw new IllegalStateException("Begin has already been called on the transaction");

		transaction.begin();
	}

}
