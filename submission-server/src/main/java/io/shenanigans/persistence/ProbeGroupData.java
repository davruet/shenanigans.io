package io.shenanigans.persistence;

import io.shenanigans.proto.Shenanigans.Submission.ProbeGroup;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table
/** JPA Data entity wrapper for a Probe Group, a collection of probes with a common MAC
 *  associated with an individual device.
 * 
 * @author dr
 *
 */
public class ProbeGroupData {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String mac;
	private String token;
	@OneToMany (cascade = {CascadeType.ALL})
	private List<ProbeReqData> probeRequests = new ArrayList<>();
	
	public ProbeGroupData(){
		
	}
	
	public ProbeGroupData(ProbeGroup group){
		group.getReqList().forEach(req -> {
			probeRequests.add(new ProbeReqData(req));
		});
		mac = group.getMac();
		token = group.getToken();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public List<ProbeReqData> getProbeRequests() {
		return probeRequests;
	}

	public void setProbeRequests(List<ProbeReqData> probeRequests) {
		this.probeRequests = probeRequests;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	
	
	
	
	
	
}
