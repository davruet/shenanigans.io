package io.shenanigans.persistence;

import io.shenanigans.concurrent.BatchProcessor;
import io.shenanigans.concurrent.BatchProcessorException;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/** Implementation of BatchProcessor<PersistEntityEvent> that persists supplied entity events using
 * the JPA.
 * 
 * Instances are not thread-safe. Best used on a single thread with a multi-threaded
 * batching frontend.
 * 
 * @author dr
 *
 */
public class JPABatchStore implements BatchProcessor<PersistEntityEvent> {


	private EntityManagerFactory m_entityManagerFactory;
	private EntityManager m_entityManager;

	public JPABatchStore(Map properties) {
	    m_entityManagerFactory = Persistence.createEntityManagerFactory("shenanigans", properties);
		m_entityManager = m_entityManagerFactory.createEntityManager();

	}

	@Override
	public void process(List<? extends PersistEntityEvent> submissions)
			throws BatchProcessorException {
		m_entityManager.getTransaction().begin();

		m_entityManager.setFlushMode(FlushModeType.AUTO);
		submissions.forEach(submission ->
			m_entityManager.persist(submission.entity));
		m_entityManager.getTransaction().commit();
		//FIXME - perform exception handling testing
	}

	public void close() {
		m_entityManager.close();
		m_entityManagerFactory.close();
	}
	

}
