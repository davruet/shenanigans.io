package io.shenanigans.server;

import java.util.List;

public interface BatchProcessor <T>{

	void process(List<? extends T> entities) throws BatchProcessorException;
	
}
