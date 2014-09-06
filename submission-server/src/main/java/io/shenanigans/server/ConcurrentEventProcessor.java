package io.shenanigans.server;

public interface ConcurrentEventProcessor<T> {

	public void processEvent(T event);
}
