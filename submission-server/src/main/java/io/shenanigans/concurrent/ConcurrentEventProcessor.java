package io.shenanigans.concurrent;

/** Interface for a service that can handle events originating from multiple threads with
 * no synchronization necessary.
 * @author dr
 *
 * @param <T>
 */
public interface ConcurrentEventProcessor<T> {

	/** Process an event. Implementing classes must provide thread-safety for this method.
	 * 
	 * @param event the event to process.
	 */
	public void processEvent(T event);
}
