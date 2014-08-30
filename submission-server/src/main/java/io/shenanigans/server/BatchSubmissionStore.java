package io.shenanigans.server;

import java.util.List;

public interface BatchSubmissionStore {

	void save(List<SubmissionReceipt> submissions) throws StoreException;
	void close();
}
