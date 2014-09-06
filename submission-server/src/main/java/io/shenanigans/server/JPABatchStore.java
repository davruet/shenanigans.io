package io.shenanigans.server;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/** Instances are not thread-safe. Best used on a single thread with a multithreaded
 * batching frontend.
 * 
 * Uses the generic <Object> template of BatchProcessor, as JPA doesn't have a base abstract
 * class for entities.
 * @author dr
 *
 */
public class JPABatchStore implements BatchProcessor<PersistEntityEvent> {


	private EntityManagerFactory m_entityManagerFactory;
	private EntityManager m_entityManager;

	public JPABatchStore() {
	    m_entityManagerFactory = Persistence.createEntityManagerFactory("shenanigans");
	    System.out.println("ENTITIES:: " + m_entityManagerFactory.getMetamodel().getEntities());
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
		//FIXME - exceptions

	}

	public void close() {
		m_entityManager.close();
		m_entityManagerFactory.close();
	}
	
	
	

}
