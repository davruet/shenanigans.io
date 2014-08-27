package io.shenanigans.server;

import java.util.HashMap;

import shenanigans.Shenanigans.Submission;

import com.lmax.disruptor.EventFactory;

public class AnnotatedSubmission {

	private Submission m_submission;
	private String m_ip;
	private HashMap<String, String> m_headers;

	public AnnotatedSubmission() {

	}

	public AnnotatedSubmission(Submission submission, String ip,
			HashMap<String, String> headers) {
		m_submission = submission;
		m_ip = ip;
		m_headers = headers;
	}

	public Submission getSubmission() {
		return m_submission;
	}

	public String getIp() {
		return m_ip;
	}

	public HashMap<String, String> getHeaders() {
		return m_headers;
	}

	public void setSubmission(Submission submission) {
		m_submission = submission;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setHeaders(HashMap<String, String> headers) {
		m_headers = headers;
	}
	
	public void copyFrom(AnnotatedSubmission another){
		m_submission = another.m_submission;
		m_ip = another.m_ip;
		m_headers = another.m_headers;
	}

	public final static EventFactory<AnnotatedSubmission> EVENT_FACTORY = new EventFactory<AnnotatedSubmission>() {
		public AnnotatedSubmission newInstance() {
			return new AnnotatedSubmission();
		}
	};

}
