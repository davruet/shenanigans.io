package io.shenanigans.server;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JPASubmissionStoreTest {

	@Test
	public void testStoreSubmissions() throws Exception{
		JPASubmissionStore store = new JPASubmissionStore();
		List<SubmissionReceipt> submissions = new ArrayList<>();
		
		for (int i = 0; i < 300; i++){
			submissions.add(makeReceipt());
		}
		store.save(submissions);
		store.close();
	}

	protected SubmissionReceipt makeReceipt() {
		SubmissionReceipt r1 = new SubmissionReceipt();
		r1.setHeaders("headers");
		r1.setIp("127.0.0.1");
		List<ProbeGroupData> groups = new ArrayList<>();
		ProbeGroupData group = new ProbeGroupData();
		group.setMac("DE:AD:BE:EF:A1:A2");
		List<ProbeReqData> requests = new ArrayList<>();
		ProbeReqData req = new ProbeReqData();
		req.setSsid("Some SSID");
		req.setReqBytes(new byte[100]);
		requests.add(req);
		group.setProbeRequests(requests);
		groups.add(group);
		r1.setProbeGroups(groups);
		return r1;
	}

}
