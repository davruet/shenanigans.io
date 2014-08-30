package io.shenanigans.server;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/** Instances are not thread-safe. Best used on a single thread with a multithreaded
 * batching frontend.
 * @author dr
 *
 */
public class JPASubmissionStore implements BatchSubmissionStore {


	private EntityManagerFactory m_entityManagerFactory;
	private EntityManager m_entityManager;

	public JPASubmissionStore() {
	    m_entityManagerFactory = Persistence.createEntityManagerFactory("shenanigans");
	    System.out.println("ENTITIES:: " + m_entityManagerFactory.getMetamodel().getEntities());
		m_entityManager = m_entityManagerFactory.createEntityManager();

	}

	@Override
	public void save(List<SubmissionReceipt> submissions)
			throws StoreException {
		m_entityManager.getTransaction().begin();

		m_entityManager.setFlushMode(FlushModeType.AUTO);
		submissions.forEach(submission ->
			m_entityManager.persist(submission));
		m_entityManager.getTransaction().commit();
		//FIXME - exceptions

	}

	@Override
	public void close() {
		m_entityManager.close();
		m_entityManagerFactory.close();
	}
	
	
	

}
