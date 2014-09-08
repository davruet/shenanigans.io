package io.shenanigans.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

/** Thread-safe, lock-free, asynchronous event processor that batches and sends events
 * to an event processor running on a single thread.
 * 
 * @author dr
 *
 * @param <T>
 */
public class AsyncConcurrentBatchingProcessor <T> implements ConcurrentEventProcessor<T>, EventHandler<T>{
	
	public static final int MAX_BATCH_SIZE_DEFAULT = 50;
	
	private int maxBatchSize = MAX_BATCH_SIZE_DEFAULT;
	private ArrayList<T> m_batch = new ArrayList<T>(maxBatchSize);
	
	// disruptor member variables. 
    private RingBuffer<T> m_buffer;
	private Disruptor<T> m_disruptor;
	private BatchProcessor<T> m_processor;
	private EventTranslatorOneArg<T,T> m_translator;
	
	@SuppressWarnings("unchecked")
	public AsyncConcurrentBatchingProcessor(BatchProcessor<T> processor, EventFactory<T> factory, EventTranslatorOneArg<T,T> translator ){
		Executor executor = Executors.newSingleThreadExecutor();
		int bufferSize = 1024;		
		m_disruptor = new Disruptor<T>(factory, bufferSize, executor);
		m_disruptor.handleEventsWith(this);
		m_buffer = m_disruptor.start();
		m_processor = processor;
		m_translator = translator;
	}
	
    public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void onEvent(T event, long sequence, boolean endOfBatch){
    	m_batch.add(event);
    	if (endOfBatch || m_batch.size() > maxBatchSize) flush(m_batch);
    }
    
    protected void flush(List<T> batch) {
		persistBatch(batch);
		m_batch.clear();
	}

	protected void persistBatch(List<T> batch) {
		try {
			m_processor.process(batch);
		} catch (BatchProcessorException e){
			LogManager.getLogger(this).error("Couldn't persist submission batch.", e);
		}
	}

	public void processEvent(T event){
		m_buffer.publishEvent(m_translator, event);
	}
	
	public void stop(){
		m_disruptor.shutdown();
	}
	
}

