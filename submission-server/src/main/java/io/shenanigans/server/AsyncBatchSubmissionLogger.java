package io.shenanigans.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class AsyncBatchSubmissionLogger {
	
	public static final int MAX_BATCH_SIZE_DEFAULT = 50;
	
	private int maxBatchSize = MAX_BATCH_SIZE_DEFAULT;
	private ArrayList<SubmissionReceipt> m_batch = new ArrayList<SubmissionReceipt>(maxBatchSize);
	
	
    public int getMaxBatchSize() {
		return maxBatchSize;
	}

	protected void handleEvent(SubmissionReceipt event, long sequence, boolean endOfBatch){
    	m_batch.add(event);
    	if (endOfBatch || m_batch.size() > maxBatchSize) flush(m_batch);
    }
    
    protected void flush(List<SubmissionReceipt> batch) {
		persistBatch(batch);
		m_batch.clear();
	}

	protected void persistBatch(List<SubmissionReceipt> batch) {
		try {
			m_store.save(batch);
		} catch (StoreException e){
			LogManager.getLogger(this).error("Couldn't persist submission batch.", e);
		}
	}

	public static void translate(SubmissionReceipt event, long sequence, SubmissionReceipt source){
    	event.copyFrom(source);
    }

    private RingBuffer<SubmissionReceipt> m_buffer;

	private Disruptor<SubmissionReceipt> m_disruptor;

	private BatchSubmissionStore m_store;
	
	
	public AsyncBatchSubmissionLogger(BatchSubmissionStore store){
		Executor executor = Executors.newSingleThreadExecutor();
		int bufferSize = 1024;
		m_disruptor = new Disruptor<>(SubmissionReceipt::new, bufferSize, executor);
		m_disruptor.handleEventsWith(this::handleEvent );
		m_buffer = m_disruptor.start();
		m_store = store;
		
	}
	
	public void logSubmission(SubmissionReceipt submission){
		m_buffer.publishEvent(AsyncBatchSubmissionLogger::translate, submission);
	}
	
	public void stop(){
		m_disruptor.shutdown();
	}
	
}

