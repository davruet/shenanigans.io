package io.shenanigans.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.http.io.NIOInputStream;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/** Handles Grizzly POSTs asynchronously.
 * 
 * @author dr
 *
 */
public class AsyncPostHandler extends HttpHandler {
	
	private ErrorHandler m_errorHandler;
	private SuccessHandler m_successHandler;
	
	public AsyncPostHandler(SuccessHandler successHandler, ErrorHandler errorHandler){
		m_errorHandler = errorHandler;
		m_successHandler = successHandler;
	}
	public void service(final Request req, final Response resp) throws Exception {
		// suspend the response, allowing for async processing
		resp.suspend();
		LogManager.getLogger(this).debug(String.format("Handling post %s", req));
		System.out.println("Request: " + req.getRequest().getHttpHeader().toString());
		int length = req.getContentLength();
		if (length > Server.MAX_POST_SIZE || length < Server.MIN_POST_SIZE){
			m_errorHandler.handleInvalidPost(null, resp, new IllegalArgumentException("Invalid content-length of " + length));
			return; // FIXME- this probably wants to be handled using an error-handling mechanism.
		}
		final ByteBuffer postBytes = ByteBuffer.allocate(req.getContentLength());
		final NIOInputStream in = req.getNIOInputStream();

		in.notifyAvailable(new ReadHandler() {
			
			public void onError(Throwable t) {
				try {
					m_errorHandler.handleInvalidPost(postBytes, resp, t);
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
				m_successHandler.handlePost(postBytes, req, resp);
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
	interface SuccessHandler {
		void handlePost(ByteBuffer postBytes, Request req, Response resp) throws IOException;
	}
	
	interface ErrorHandler {
		void handleInvalidPost(ByteBuffer postBytes, Response resp, Throwable t) throws IOException;
	}
	

}