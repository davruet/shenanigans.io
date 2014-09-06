package io.shenanigans.server;

import io.shenanigans.proto.Shenanigans.ServerStatusQuery;
import io.shenanigans.proto.Shenanigans.ServerStatusResponse;
import io.shenanigans.proto.Shenanigans.ServerStatusResponse.StatusCode;
import io.shenanigans.proto.Shenanigans.Submission;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Identity submission server for shenanigans.io
 * @author davruet
 * 
 * TODO - implement request and stats logging
 * TODO - 
 * 
 */
public class Server 
{
	
	public static final String HOST = "localhost";
	public static final int PORT = 8000;
	public static final int MAX_POST_SIZE = 100000; // FIXME - revisit this
	public static final int MIN_POST_SIZE = 15;
	
    public static void main( String[] args ) throws IOException, InterruptedException {
        final HttpServer server = new HttpServer();
        
        final NetworkListener networkListener = new NetworkListener(
                "secured-listener",
                HOST,
                PORT);
        
        // Enable SSL on the listener
        networkListener.setSecure(true);
        networkListener.setSSLEngineConfig(initializeSSL());
        
        
        server.addListener(networkListener);
        
        // fire up the backend
        JPABatchStore store = new JPABatchStore();
    	AsyncConcurrentBatchingProcessor<PersistEntityEvent> batchPersister = new AsyncConcurrentBatchingProcessor<PersistEntityEvent>(
    			store,
    			PersistEntityEvent::new,
    			PersistEntityEvent::translate
    			);
        
        final ServerConfiguration config = server.getServerConfiguration();
        
        config.setMaxPostSize(MAX_POST_SIZE);
        config.addHttpHandler(new CertificateHandler(batchPersister, "src/resource/cert-template.pdf"), "/submitFingerprint");
        config.addHttpHandler(new VersionCheckHandler(batchPersister), "/versionCheck");
        server.getListeners();
        try {
        	server.start();
        	while (true){
        		Thread.sleep(1000);
        	}
        } finally {
        	store.close();
        	server.shutdownNow();
        }
    }
    
    private static SSLEngineConfigurator initializeSSL(){
    	
        SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();

        //|Set key store
        ClassLoader cl = Server.class.getClassLoader();/*
        URL cacertsUrl = cl.getResource("ssltest-cacerts.jks");
        if(cacertsUrl != null){
            sslContextConfig.setTrustStoreFile(cacertsUrl.getFile());
            sslContextConfig.setTrustStorePass("changeit");
        }*/

        //|Set trust store
        URL keystoreUrl = cl.getResource("ssltest-keystore.jks"); // FIXME - use real key
        if(keystoreUrl != null){
            sslContextConfig.setKeyStoreFile(keystoreUrl.getFile());
            sslContextConfig.setKeyStorePass("changeit"); // FIXME - need mechanism for password entry on server.
        } else {
        	throw new RuntimeException("No keys!!");
        }

        //|Create SSLEngine configurator
        return new SSLEngineConfigurator(sslContextConfig.createSSLContext(), false, false, false);
    }

}

abstract class AsyncPostHandler extends HttpHandler {
	public void service(final Request req, final Response resp) throws Exception {
		// suspend the response, allowing for async processing
		resp.suspend();
		System.out.println("Request: " + req.getRequest().getHttpHeader().toString());
		int length = req.getContentLength();
		if (length > Server.MAX_POST_SIZE || length < Server.MIN_POST_SIZE){
			handleInvalidPost(null, resp, new IllegalArgumentException("Invalid content-length of " + length));
			return; // FIXME- this probably wants to be handled using an error-handling mechanism.
		}
		final ByteBuffer postBytes = ByteBuffer.allocate(req.getContentLength());
		final NIOInputStream in = req.getNIOInputStream();

		in.notifyAvailable(new ReadHandler() {
			
			public void onError(Throwable t) {
				try {
					handleInvalidPost(postBytes, resp, t);
				} catch (IOException e) {
					e.printStackTrace(); // FIXME
				}
				resp.resume();
			}
			
			public void onDataAvailable() throws Exception {
				addToBuffer(in, postBytes);
				in.notifyAvailable(this);
			}
			
			public void onAllDataRead() throws Exception {

				addToBuffer(in, postBytes);
				postBytes.flip();
									
				handlePost(postBytes, req, resp);

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
	
	/** handle the post after all data is read.
	 * The passed-in response has been suspended, therefore the implementing class is responsible for 
	 * calling resume() or canceling it.
	 * @param postBytes
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	protected void handlePost(ByteBuffer postBytes, Request req, Response resp) throws IOException{
		resp.resume();
	}
	
	protected void handleInvalidPost(ByteBuffer postBytes, Response resp, Throwable t) throws IOException{
		
	}
}

class CertificateHandler extends AsyncPostHandler {
	
	private CertTemplate m_template;
	private ConcurrentEventProcessor<PersistEntityEvent> m_processor;
	
	public CertificateHandler(ConcurrentEventProcessor<PersistEntityEvent> processor, String certTemplate) throws IOException {
		m_template = new CertTemplate(certTemplate);
		m_processor = processor;
	}

	/* TODO - put this in a thread pool and let the http server get back to its work. */
	@Override
	protected void handlePost(ByteBuffer postBytes, Request req, Response resp) throws IOException{
		try {
			// Parse the submission
			Submission submission = Submission.parseFrom(ByteString.copyFrom(postBytes));
			
			getAllHeaders(req);
			m_processor.processEvent(new PersistEntityEvent(
					new SubmissionReceipt(
							submission, req.getRemoteAddr(), new Date(), getAllHeaders(req))));
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
				handleInvalidPost(postBytes, resp, e);
			} finally {
				out.close();
				doc.close();
			}
			
		} catch (InvalidProtocolBufferException e) {
			handleInvalidPost(postBytes, resp, e);
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
	
	//TODO - make this report and handle errors in a way that will help debugging
	//TODO - test error handling!
	@Override
	protected void handleInvalidPost(ByteBuffer postBytes, Response resp, Throwable t) throws IOException{
		// FIXME let's actually add the submission, not letting the submitter
		//really know whether it was successful. Randomly, let's throttle the response too.
		LogManager.getLogger(this).warn("Invalid submission.", t);
		t.printStackTrace();
		resp.sendError(300);
		resp.finish();
	}
	
}

class PersistEntityEvent {
	
	
	public PersistEntityEvent(Object entity) {
		this.entity = entity;
	}
	
	public PersistEntityEvent(){
		
	}

	protected Object entity;
	
	static void translate(PersistEntityEvent source, long id, PersistEntityEvent another){ // FIXME this should be static
		// FIXME - also, if we aren't doing a deep copy of the entity, are we doing something bad?
		source.entity = another.entity;
	}
}

class VersionCheckHandler extends AsyncPostHandler {

	private ConcurrentEventProcessor<PersistEntityEvent> m_processor;
	private StatusCode serverStatus = StatusCode.READY;
	private HashSet<String> m_compatibleVersions = new HashSet<>();
	
	public VersionCheckHandler(
			ConcurrentEventProcessor<PersistEntityEvent> processor) {
		super();
		m_processor = processor;
		m_compatibleVersions.add("0.1a");
	}

	@Override
	protected void handlePost(ByteBuffer postBytes, Request req, Response resp)
			throws IOException {
		try {
			ServerStatusQuery query = ServerStatusQuery.parseFrom(ByteString.copyFrom(postBytes));
			m_processor.processEvent(new PersistEntityEvent(new ServerStatusQueryData(query)));
			ServerStatusResponse.Builder response = ServerStatusResponse.newBuilder();
			response.setServerDate(new Date().getTime());
			response.setStatusCode(getStatusCode(query));
			
			NIOOutputStream out = resp.getNIOOutputStream();
			try {
				response.build().writeTo(out);
			} finally {
				out.close();
			}
					
		} finally {
			resp.resume();
		}
	}

	private StatusCode getStatusCode(ServerStatusQuery query) {
		if (serverStatus != StatusCode.READY) return serverStatus;
		if (isVersionOK(query.getVersion())) return StatusCode.READY;
		else return StatusCode.CLIENT_MUST_UPGRADE;
	}

	private boolean isVersionOK(String version) {
		return m_compatibleVersions .contains(version);
	}

	@Override
	protected void handleInvalidPost(ByteBuffer postBytes, Response resp,
			Throwable t) throws IOException {
		// TODO Auto-generated method stub
		super.handleInvalidPost(postBytes, resp, t);
	}

	

    
}

