package io.shenanigans.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class SubmissionLogger {
	
	public static final int MAX_BATCH_SIZE_DEFAULT = 50;
	
	private int maxBatchSize = MAX_BATCH_SIZE_DEFAULT;
	private ArrayList<AnnotatedSubmission> m_batch = new ArrayList<AnnotatedSubmission>(maxBatchSize);
	
	
    public int getMaxBatchSize() {
		return maxBatchSize;
	}

	protected void handleEvent(AnnotatedSubmission event, long sequence, boolean endOfBatch){
    	m_batch.add(event);
    	if (endOfBatch || m_batch.size() > maxBatchSize) flush(m_batch);
    }
    
    protected void flush(List<AnnotatedSubmission> batch) {
		persistBatch(batch);
		m_batch.clear();
	}

	protected void persistBatch(List<AnnotatedSubmission> batch) {
		// TODO Auto-generated method stub
		
	}

	public static void translate(AnnotatedSubmission event, long sequence, AnnotatedSubmission source){
    	event.copyFrom(source);
    }

    private RingBuffer<AnnotatedSubmission> m_buffer;

	private Disruptor<AnnotatedSubmission> m_disruptor;
	
	public SubmissionLogger(){
		Executor executor = Executors.newSingleThreadExecutor();
		int bufferSize = 1024;
		m_disruptor = new Disruptor<>(AnnotatedSubmission::new, bufferSize, executor);
		m_disruptor.handleEventsWith(this::handleEvent );
		m_buffer = m_disruptor.start();
		
	}
	
	public void logSubmission(AnnotatedSubmission submission){
		m_buffer.publishEvent(SubmissionLogger::translate, submission);
	}
	
	public void stop(){
		m_disruptor.shutdown();
	}
	
}

