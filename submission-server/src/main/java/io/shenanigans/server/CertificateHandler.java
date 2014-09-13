package io.shenanigans.server;

import io.shenanigans.concurrent.ConcurrentEventProcessor;
import io.shenanigans.persistence.PersistEntityEvent;
import io.shenanigans.persistence.SubmissionReceipt;
import io.shenanigans.proto.Shenanigans.Submission;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.glassfish.grizzly.http.io.NIOOutputStream;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

class CertificateHandler implements AsyncPostHandler.SuccessHandler {
	
	private static final String PDF_CONTENT_TYPE = "application/pdf";
	private CertTemplate m_template;
	private ConcurrentEventProcessor<PersistEntityEvent> m_processor;
	private AsyncPostHandler.ErrorHandler m_errorHandler;
	
	public CertificateHandler(ConcurrentEventProcessor<PersistEntityEvent> processor, String certTemplate, AsyncPostHandler.ErrorHandler errorHandler) throws IOException {
		m_template = new CertTemplate(certTemplate);
		m_processor = processor;
		m_errorHandler = errorHandler;
	}

	/* TODO - put this in a thread pool and let the http server get back to its work. */
	@Override
	public void handlePost(ByteBuffer postBytes, Request req, Response resp) throws IOException{
		try {
			// Parse the submission
			Submission submission = Submission.parseFrom(ByteString.copyFrom(postBytes));
			m_processor.processEvent(new PersistEntityEvent(
					new SubmissionReceipt(
							submission, req.getRemoteAddr(), submission.getDate(),
							new Date().getTime(),  req.getRequest().getHttpHeader().toString())));
			int size = submission.getGroupList().size();
			
			List<String> macs = new ArrayList<String>(size);
			List<String> ssids = new ArrayList<String>(size);
			
			// Build lists of MACs and SSIDs
			submission.getGroupList().stream().forEach(group ->{
				ssids.add(group.getReqList().stream().map(r -> r.getSsid())
						.collect(Collectors.joining(", ")));
				
				macs.add(group.getMac());
			});
			
			// Fill out the template, and save it to the response
			PDDocument doc = m_template.fillTemplate(macs, ssids);
			NIOOutputStream out = resp.getNIOOutputStream();
			try {
				resp.setContentType(PDF_CONTENT_TYPE);
				doc.save(out);
			} catch (COSVisitorException e){
				m_errorHandler.handleInvalidPost(postBytes, resp, e);
			} finally {
				out.close();
				doc.close();
			}
			
		} catch (InvalidProtocolBufferException e) {
			m_errorHandler.handleInvalidPost(postBytes, resp, e);
		} finally {
			resp.resume();
		}
		
	}

	protected HashMap<String, String> getAllHeaders(Request req) {
		HashMap<String, String> headers = new HashMap<String, String>();
		for (String s : req.getHeaderNames()){
			headers.put(s, req.getHeader(s));
		}
		return headers;
	}

}