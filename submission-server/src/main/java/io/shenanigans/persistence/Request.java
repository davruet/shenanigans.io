package io.shenanigans.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Request {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="CLIENT_DATE")
	private long clientDate;
	
	@Column(name="SERVER_DATE")
	private long serverDate;
	
	private String token;
	
	private String ip;
	
	private String headers;
	
	public Request(){}

	public Request(long clientDate, long serverDate, String token, String ip, String headers) {
		super();
		this.clientDate = clientDate;
		this.token = token;
		this.ip = ip;
		this.headers = headers;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getDate() {
		return clientDate;
	}

	public void setDate(long date) {
		this.clientDate = date;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}
	
	
	
	
	
	

	
}
