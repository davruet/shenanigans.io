package io.shenanigans.concurrent;

import java.util.List;

/** Interface used by {@link io.shenanigans.concurrent.AsyncConcurrentBatchingProcessor} to
 * process batches of submitted events. Implementations need not be thread-safe, as the
 * batching processor serializes invocations of process().
 * 
 * @author dr
 *
 * @param <T>
 */
public interface BatchProcessor <T>{

	/** Process the list of events.
	 * 
	 * @param events the events to process. 
	 * @throws BatchProcessorException if an error occurs during processing.
	 */
	void process(List<? extends T> events) throws BatchProcessorException;
	
}
