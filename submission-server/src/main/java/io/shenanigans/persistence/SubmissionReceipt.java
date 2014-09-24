package io.shenanigans.persistence;

import io.shenanigans.proto.Shenanigans.Submission;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.lmax.disruptor.EventFactory;

@Entity
@Table(name="submission")
public class SubmissionReceipt {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne(cascade = {CascadeType.ALL})
	private Request request;
	
	@Transient
	private Submission submission;

	@OneToMany(cascade = {CascadeType.ALL})
	private List<ProbeGroupData> probeGroups = new ArrayList<>();
	

	public SubmissionReceipt() {

	}

	public SubmissionReceipt(Submission submission, String ip, long dateSubmitted, long serverDate,
			String headers) {
		this.submission = submission;
		this.request = new Request(dateSubmitted, serverDate, submission.getToken(), ip, headers);
		
		submission.getGroupList().forEach(group ->
			probeGroups.add(new ProbeGroupData(group)));
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ProbeGroupData> getProbeGroups() {
		return probeGroups;
	}

	public void setProbeGroups(List<ProbeGroupData> probeGroups) {
		this.probeGroups = probeGroups;
	}

	public Submission getSubmission() {
		return submission;
	}

	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	public final static EventFactory<SubmissionReceipt> EVENT_FACTORY = new EventFactory<SubmissionReceipt>() {
		public SubmissionReceipt newInstance() {
			return new SubmissionReceipt();
		}
	};


	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	
	

}
