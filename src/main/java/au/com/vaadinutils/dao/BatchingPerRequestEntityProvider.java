/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.com.vaadinutils.dao;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.BatchableEntityProvider;
import com.vaadin.addon.jpacontainer.CachingEntityProvider;
import com.vaadin.addon.jpacontainer.provider.BatchUpdatePerformedEvent;
import com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;

/**
 * A very simple implementation of {@link BatchableEntityProvider} with caching
 * support that simply passes itself to the {@link BatchUpdateCallback}. No data
 * consistency checks are performed.
 * 
 * @see CachingMutableLocalEntityProvider
 * @see BatchableLocalEntityProvider
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class BatchingPerRequestEntityProvider<T> extends CachingMutableLocalEntityProvider<T> implements
		BatchableEntityProvider<T>, CachingEntityProvider<T>
{

	private static final long serialVersionUID = 9174163487778140520L;

//	EntityManager em = EntityManagerProvider.getEntityManager();
	/**
	 * Creates a new <code>CachingBatchableLocalEntityProvider</code>. The
	 * entity manager must be set using
	 * {@link #setEntityManager(javax.persistence.EntityManager) }.
	 * 
	 * @param entityClass
	 *            the entity class (must not be null).
	 */
	public BatchingPerRequestEntityProvider(Class<T> entityClass)
	{
		super(entityClass);
		setCacheEnabled(true);
	}


	static private ThreadLocal<Integer> updating = new ThreadLocal<Integer>();
	
	public void batchUpdate(final BatchUpdateCallback<T> callback) throws UnsupportedOperationException
	{
		assert callback != null : "callback must not be null";
		if (updating.get()== null)
		{
			updating.set(1);
			getEntityManager().getTransaction().commit();
		}
		setFireEntityProviderChangeEvents(false);
		try
		{
			runInTransaction(new Runnable()
			{

				public void run()
				{
					callback.batchUpdate(BatchingPerRequestEntityProvider.this);
					
				}
			});
		}
		finally
		{
			int count = updating.get()-1;
			updating.set(count);
			if (count == 0)
			{
				getEntityManager().getTransaction().begin();
				updating.set(null);
			}
			setFireEntityProviderChangeEvents(true);
		}
		fireEntityProviderChangeEvent(new BatchUpdatePerformedEvent<T>(this));
	}

//	protected EntityManager doGetEntityManager() throws IllegalStateException
//	{
//		return em;
//	}
//
//	public EntityManager getEntityManager()
//	{
//		return em;
//	}

	protected EntityManager doGetEntityManager() throws IllegalStateException
	{
		return EntityManagerProvider.getEntityManager();
	}

	public EntityManager getEntityManager()
	{
		return EntityManagerProvider.getEntityManager();
	}
	
//	  protected void runInTransaction(Runnable operation) {
//	        assert operation != null : "operation must not be null";
//	        if (isTransactionsHandledByProvider()) {
//	            EntityTransaction et = getEntityManager().getTransaction();
//	            if (et.isActive()) {
//	                // The transaction has been started outside of this method
//	                // and should also be committed/rolled back outside of
//	                // this method
//	                operation.run();
//	            } else {
//	                try {
//	                    et.begin();
//	                    operation.run();
//	                    et.commit();
//	                } finally {
//	                    if (et.isActive()) {
//	                        et.rollback();
//	                    }
//	                }
//	            }
//	        } else {
//	            operation.run();
//	        }
//	    }


}
