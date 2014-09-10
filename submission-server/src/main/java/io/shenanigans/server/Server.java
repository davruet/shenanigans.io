package io.shenanigans.server;

import io.shenanigans.concurrent.AsyncConcurrentBatchingProcessor;
import io.shenanigans.persistence.JPABatchStore;
import io.shenanigans.persistence.PersistEntityEvent;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.EncodingFilter;
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
	private static final String CERT_TEMPLATE_PATH = "/cert-template.pdf";
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 8000;
	public static final int MAX_POST_SIZE = 100000; // FIXME - revisit this
	public static final int MIN_POST_SIZE = 15;
	private static final String CONFIG_FILE_NAME = "etc/server.properties";
	
	private static final String PROPERTY_PORT = "server.port";
	private static final String PROPERTY_HOST = "server.host";
	private static final String PROPERTY_KEYSTORE_FILE = "keystore.file";
	private static final String PROPERTY_KEYSTORE_PASS = "keystore.password";


	private HttpServer m_httpServer;
	private JPABatchStore m_store;
	private static Server g_server;
	private AsyncConcurrentBatchingProcessor<PersistEntityEvent> m_batchPersister;
	private PropertiesConfiguration m_properties;

	public Server(PropertiesConfiguration properties) throws IOException {
		m_properties = properties;
		m_httpServer = new HttpServer();
		
		int port = properties.getInt(PROPERTY_PORT, DEFAULT_PORT);
		String host = properties.getString(PROPERTY_HOST, DEFAULT_HOST);
		final NetworkListener networkListener = new NetworkListener(
				NET_LISTENER_NAME,
				host,
				port);

		// Enable SSL on the listener
		networkListener.setSecure(true);
		networkListener.setSSLEngineConfig(makeSSLConfig(m_properties));

		CompressionConfig compressionConfig =
		        networkListener.getCompressionConfig();
		compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.ON); // the mode
		compressionConfig.setCompressionMinSize(100); // the min amount of bytes to compress
		compressionConfig.setCompressableMimeTypes("text/plain", "text/html", "application/x-protobuf", "application/pdf"); // the mime types to compress
		
		m_httpServer.addListener(networkListener);
		

		// Create a concurrent, nonblocking, asynchronous, batching JPA-based store for persistence
		// of request data. Async is OK, as persistence failures do not need to be handled by the client.
		m_store = new JPABatchStore(ConfigurationConverter.getMap(m_properties));
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


	private static SSLEngineConfigurator makeSSLConfig(PropertiesConfiguration properties){

		SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();

		
		sslContextConfig.setKeyStoreFile(properties.getString(PROPERTY_KEYSTORE_FILE));
		sslContextConfig.setKeyStorePass(properties.getString(PROPERTY_KEYSTORE_PASS)); 
		
		return new SSLEngineConfigurator(sslContextConfig.createSSLContext(), false, false, false);
	}

	public static void main( String[] args ) throws IOException, InterruptedException, ConfigurationException {
		g_server = new Server(new PropertiesConfiguration(CONFIG_FILE_NAME));
		g_server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				g_server.stop();
			}
		}); 
	}

}

