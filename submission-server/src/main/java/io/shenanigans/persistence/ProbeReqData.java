package io.shenanigans.server;

import io.shenanigans.proto.Shenanigans.Submission.ProbeGroup.ProbeReq;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class ProbeReqData {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String ssid;
	private byte[] reqBytes;
	
	public ProbeReqData(ProbeReq req){
		ssid = req.getSsid();
		reqBytes = req.getReqBytes().toByteArray();
	}
	
	public ProbeReqData(){
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public byte[] getReqBytes() {
		return reqBytes;
	}

	public void setReqBytes(byte[] reqBytes) {
		this.reqBytes = reqBytes;
	}
	
	

}
