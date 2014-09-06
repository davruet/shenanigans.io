package io.shenanigans.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lmax.disruptor.EventTranslatorOneArg;

public class AsyncConcurrentBatchingProcessorTest {

	private CountDownLatch m_latch;
	private AsyncConcurrentBatchingProcessor<Object> m_processor;
	private List<List<Object>>m_receivedEvents = new ArrayList<List<Object>>();
	
	@Before
	public void init(){
		m_latch = new CountDownLatch(1);
		BatchProcessor<Object> bp = new BatchProcessor<Object>() {

			@Override
			public void process(List<? extends Object> batch) throws BatchProcessorException {
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
		m_processor = new AsyncConcurrentBatchingProcessor<Object>(bp, Object::new, new EventTranslatorOneArg<Object, Object>() {
			public void translateTo(Object event, long sequence, Object arg0) {};
		});
	}
	
	@After
	public void stop(){
		m_processor.stop();
	}
	
	@Test
	public void testSubmission() throws Exception {
		
		m_processor.processEvent(new Object());
	
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
			m_processor.processEvent(new Object());
		}
		awaitCompletion();
		for (List<?> list : m_receivedEvents){
			System.out.println(list.size());
		}
		Assert.assertTrue("Number of invocations wasn't greater than number of batches. Batching doesn't seem to be working at all.",
				m_receivedEvents.size() < count );
	}
}
