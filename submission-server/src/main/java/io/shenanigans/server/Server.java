package io.shenanigans.server;

import io.shenanigans.concurrent.AsyncConcurrentBatchingProcessor;
import io.shenanigans.persistence.JPABatchStore;
import io.shenanigans.persistence.PersistEntityEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;


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

	private static final String NET_LISTENER_NAME = "secured-listener";
	private static final String PATH_VERSION_CHECK = "/versionCheck";
	private static final String PATH_SUBMIT = "/submitFingerprint";
	private static final String CERT_TEMPLATE_PATH = "src/resource/cert-template.pdf";
	public static final String HOST = "localhost";
	public static final int PORT = 8000;
	public static final int MAX_POST_SIZE = 100000; // FIXME - revisit this
	public static final int MIN_POST_SIZE = 15;

	private HttpServer m_httpServer;
	private JPABatchStore m_store;
	private static Server g_server;
	private AsyncConcurrentBatchingProcessor<PersistEntityEvent> m_batchPersister;

	public Server(String host, int port) throws IOException {
		m_httpServer = new HttpServer();

		final NetworkListener networkListener = new NetworkListener(
				NET_LISTENER_NAME,
				HOST,
				PORT);

		// Enable SSL on the listener
		networkListener.setSecure(true);
		networkListener.setSSLEngineConfig(makeSSLConfig());

		m_httpServer.addListener(networkListener);

		// Create a concurrent, nonblocking, asynchronous, batching JPA-based store for persistence
		// of request data. Async is OK, as persistence failures do not need to be handled by the client.
		m_store = new JPABatchStore();
		m_batchPersister = new AsyncConcurrentBatchingProcessor<PersistEntityEvent>(
				m_store,
				PersistEntityEvent::new,
				PersistEntityEvent::translate
				);

		final ServerConfiguration config = m_httpServer.getServerConfiguration();

		config.setMaxPostSize(MAX_POST_SIZE);
		AsyncPostHandler.ErrorHandler errorHandler = 
				(ByteBuffer postBytes, Response resp, Throwable t) -> {
					LogManager.getLogger(this).warn("Invalid submission.", t);
					t.printStackTrace();
					resp.sendError(300);
					resp.finish();
				};

		AsyncPostHandler certHandler = 
				new AsyncPostHandler(new CertificateHandler(m_batchPersister, CERT_TEMPLATE_PATH, errorHandler),
						errorHandler);
		AsyncPostHandler versionCheckHandler =
				new AsyncPostHandler(new VersionCheckHandler(m_batchPersister), errorHandler);
		config.addHttpHandler(certHandler, PATH_SUBMIT);
		config.addHttpHandler(versionCheckHandler, PATH_VERSION_CHECK);
	}

	public void start() throws IOException{
		m_httpServer.start();
	}

	public void stop(){
		m_store.close();
		m_batchPersister.stop();
		m_httpServer.shutdownNow();
	}


	private static SSLEngineConfigurator makeSSLConfig(){

		SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();

		//Set key store
		ClassLoader cl = Server.class.getClassLoader();/*
        URL cacertsUrl = cl.getResource("ssltest-cacerts.jks");
        if(cacertsUrl != null){
            sslContextConfig.setTrustStoreFile(cacertsUrl.getFile());
            sslContextConfig.setTrustStorePass("changeit");
        }*/

		//|Set trust store
		//URL keystoreUrl = cl.getResource("etc/keystore.jks"); // FIXME - use real key
		//if(keystoreUrl != null){
			
			sslContextConfig.setKeyStoreFile("etc/keystore.jks");
			sslContextConfig.setKeyStorePass("changeit"); // FIXME - need mechanism for password entry on server.
		//} else {
		//	throw new RuntimeException("No keys!!");
		//}

		//|Create SSLEngine configurator
		return new SSLEngineConfigurator(sslContextConfig.createSSLContext(), false, false, false);
	}

	public static void main( String[] args ) throws IOException, InterruptedException {
		g_server = new Server(HOST, PORT);
		g_server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				g_server.stop();
			}
		}); 
	}

}

