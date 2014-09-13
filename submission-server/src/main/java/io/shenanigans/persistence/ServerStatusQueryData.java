package io.shenanigans.persistence;

import io.shenanigans.proto.Shenanigans.ServerStatusQuery;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="status_query")
public class ServerStatusQueryData {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String version;
	
	@OneToOne(cascade = {CascadeType.ALL})
	private Request request;
	

	public ServerStatusQueryData(){}
	
	public ServerStatusQueryData(ServerStatusQuery proto, String ip, String headers, long serverDate) {
		version = proto.getVersion();
		request = new Request( proto.getDate(), serverDate, proto.getToken(), ip, headers);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
	
	
	
	
}
