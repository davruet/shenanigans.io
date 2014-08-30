package io.shenanigans.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SubmissionLoggerTest {

	private CountDownLatch m_latch;
	private AsyncBatchSubmissionLogger m_logger;
	private List<List<SubmissionReceipt>>m_receivedEvents = new ArrayList<List<SubmissionReceipt>>();
	
	@Before
	public void init(){
		m_latch = new CountDownLatch(1);
		BatchSubmissionStore store = new BatchSubmissionStore() {
			
			@Override
			public void save(List<SubmissionReceipt> submissions) throws StoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
		};
		m_logger = new AsyncBatchSubmissionLogger(store){

			@Override
			protected void persistBatch(List<SubmissionReceipt> batch) {
				m_receivedEvents.add(new ArrayList<>(batch));
				for (int i = 0; i < batch.size(); i++){
					m_latch.countDown();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		};
	}
	@After
	public void stop(){
		m_logger.stop();
	}
	
	@Test
	public void testSubmission() throws Exception {
		
		SubmissionReceipt submission = new SubmissionReceipt();
		m_logger.logSubmission(submission);
	
		awaitCompletion();
		Assert.assertTrue(m_receivedEvents.get(0).size() == 1);
		
	}
	protected void awaitCompletion() throws InterruptedException {
		Assert.assertTrue(m_latch.await(1000, TimeUnit.MILLISECONDS)); // if this takes more than 100 ms something is very weird.
	}
	
	@Test
	public void testBatching() throws Exception{
		int count = 100;
		m_latch = new CountDownLatch(count);
		for (int i = 0; i < count; i++){
			m_logger.logSubmission(new SubmissionReceipt());
		}
		awaitCompletion();
		for (List<?> list : m_receivedEvents){
			System.out.println(list.size());
		}
		Assert.assertTrue("Number of invocations wasn't greater than number of batches. Batching doesn't seem to be working at all.",
				m_receivedEvents.size() < count );
	}
}
