package io.shenanigans.server;

import io.shenanigans.concurrent.ConcurrentEventProcessor;
import io.shenanigans.persistence.PersistEntityEvent;
import io.shenanigans.persistence.ServerStatusQueryData;
import io.shenanigans.proto.Shenanigans.ServerStatusQuery;
import io.shenanigans.proto.Shenanigans.ServerStatusResponse;
import io.shenanigans.proto.Shenanigans.ServerStatusResponse.StatusCode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.glassfish.grizzly.http.io.NIOOutputStream;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.google.protobuf.ByteString;

class VersionCheckHandler implements AsyncPostHandler.SuccessHandler {

	private static final String PROTOBUF_CONTENT_TYPE = "application/x-protobuf";
	private static final String PROPERTY_SERVER_STATUS = "server.status";

	
	private ConcurrentEventProcessor<PersistEntityEvent> m_processor;
	private HashSet<String> m_compatibleVersions = new HashSet<>();
	private PropertiesConfiguration m_config;
	
	public VersionCheckHandler(
			ConcurrentEventProcessor<PersistEntityEvent> processor, PropertiesConfiguration config) {
		super();
		m_processor = processor;
		m_config = config;
		m_compatibleVersions.add("0.1a");
	}

	@Override
	public void handlePost(ByteBuffer postBytes, Request req, Response resp)
			throws IOException {
		try {
			ServerStatusQuery query = ServerStatusQuery.parseFrom(ByteString.copyFrom(postBytes));
			m_processor.processEvent(new PersistEntityEvent(
					new ServerStatusQueryData(query, req.getRemoteAddr(),  req.getRequest().getHttpHeader().toString(), new Date().getTime())));
			ServerStatusResponse.Builder response = ServerStatusResponse.newBuilder();
			response.setServerDate(new Date().getTime());
			response.setStatusCode(getStatusCode(query));
			
			NIOOutputStream out = resp.getNIOOutputStream();
			try {
				resp.setContentType(PROTOBUF_CONTENT_TYPE);
				response.build().writeTo(out);
			} finally {
				out.close();
			}
					
		} finally {
			resp.resume();
		}
	}

	private StatusCode getStatusCode(ServerStatusQuery query) {
		StatusCode serverStatus = StatusCode.valueOf(
				m_config.getInt(PROPERTY_SERVER_STATUS, StatusCode.READY_VALUE));
		if (serverStatus != StatusCode.READY) return serverStatus;
		if (isVersionOK(query.getVersion())) return StatusCode.READY;
		else return StatusCode.CLIENT_MUST_UPGRADE;
	}

	private boolean isVersionOK(String version) {
		return m_compatibleVersions.contains(version);
	}

	

    
}