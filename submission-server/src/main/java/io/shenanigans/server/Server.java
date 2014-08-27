package io.shenanigans.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.http.io.NIOInputStream;
import org.glassfish.grizzly.http.io.NIOOutputStream;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;

import shenanigans.Shenanigans.Submission;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Identity submission server for shenanigans.io
 * @author davruet
 * 
 */
public class Server 
{
	
	public static final String HOST = "localhost";
	public static final int PORT = 8000;
	public static final int MAX_POST_SIZE = 100000; // FIXME - revisit this
	public static final int MIN_POST_SIZE = 15;
	
    public static void main( String[] args ) throws IOException, InterruptedException {
        final HttpServer server = HttpServer.createSimpleServer(".", PORT);
        final ServerConfiguration config = server.getServerConfiguration();
        config.setMaxPostSize(MAX_POST_SIZE);
        config.addHttpHandler(new CertificateHandler("src/resource/cert-template.pdf"), "/submitFingerprint");
        
        try {
        	server.start();
        	while (true){
        		Thread.sleep(1000);
        	}
        } finally {
        	server.shutdownNow();
        }
    }
}

class CertificateHandler extends HttpHandler {
	
	private CertTemplate m_template;
	private SubmissionLogger submissionLogger = new SubmissionLogger();
	
	public CertificateHandler(String certTemplate) throws IOException {
		m_template = new CertTemplate(certTemplate);
	}

	@Override
	public void service(final Request req, final Response resp) throws Exception {
		final NIOInputStream in = req.getNIOInputStream();
		// suspend the response, allowing for async processing
		resp.suspend();
		
		int length = req.getContentLength();
		if (length > Server.MAX_POST_SIZE || length < Server.MIN_POST_SIZE){
			handleInvalidSubmission(resp, new IllegalArgumentException("Invalid content-length of " + length));
			return; // FIXME- this probably wants to be handled using an error-handling mechanism.
		}
		final ByteBuffer postBytes = ByteBuffer.allocate(length);
		
		in.notifyAvailable(new ReadHandler() {
			
			public void onError(Throwable t) {
				handleInvalidSubmission(resp, t);
				resp.resume();
			}
			
			public void onDataAvailable() throws Exception {
				addToBuffer(in, postBytes);
				in.notifyAvailable(this);
			}
			
			public void onAllDataRead() throws Exception {
				try {
					// 
					addToBuffer(in, postBytes);
					postBytes.flip();
										
					handleSubmission(postBytes, req, resp);
				} finally {
					resp.resume();
				}
			}
		});
		
	}
	
	public void addToBuffer(NIOInputStream in, ByteBuffer postBytes){
		Buffer buffer = in.readBuffer();
		ByteBuffer bb = buffer.toByteBuffer();
		try {
			postBytes.put(bb);			
		} finally {
			buffer.tryDispose();
		}
	}
	
	/* TODO - put this in a thread pool and let the http server get back to its work. */
	private void handleSubmission(ByteBuffer postBytes, Request req, Response resp) throws IOException{
		try {
			// Parse the submission
			Submission submission = Submission.parseFrom(ByteString.copyFrom(postBytes));
			
			getAllHeaders(req);
			
			submissionLogger.logSubmission(new AnnotatedSubmission(submission, req.getRemoteAddr(), getAllHeaders(req)));
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
				doc.save(out);
			} catch (COSVisitorException e){
				handleInvalidSubmission(resp, e);
			} finally {
				out.close();
			}
			
		} catch (InvalidProtocolBufferException e) {
			handleInvalidSubmission(resp, e);
		}
		
	}

	protected HashMap<String, String> getAllHeaders(Request req) {
		HashMap<String, String> headers = new HashMap<String, String>();
		for (String s : req.getHeaderNames()){
			headers.put(s, req.getHeader(s));
		}
		return headers;
	}
	
	private void handleInvalidSubmission(Response resp, Throwable t){
		// FIXME let's actually add the submission, not letting the submitter
		//really know whether it was successful. Randomly, let's throttle the response too.
		LogManager.getLogger(this).warn("Invalid sumission.", t);
		
	}
	
}
