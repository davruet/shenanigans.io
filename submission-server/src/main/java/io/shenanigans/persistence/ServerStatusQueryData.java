package io.shenanigans.persistence;

import io.shenanigans.proto.Shenanigans.ServerStatusQuery;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class ServerStatusQueryData {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private Date date;
	
	private String token;
	
	private String version;

	public ServerStatusQueryData(){}
	
	public ServerStatusQueryData(ServerStatusQuery proto) {
		date = new Date(proto.getDate());
		token = proto.getToken();
		version = proto.getVersion();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
