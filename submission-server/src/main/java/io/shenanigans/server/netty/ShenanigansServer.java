package io.shenanigans.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class ShenanigansServer {

	private static final int PORT = Integer.parseInt(System.getProperty("shenanigans.port", "8000"));

	public ShenanigansServer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		final SslContext sslCtx;
		SelfSignedCertificate ssc = new SelfSignedCertificate();
		
		sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
	               .channel(NioServerSocketChannel.class)
	               .handler(new LoggingHandler(LogLevel.DEBUG))
	               .childHandler(new ShenanigansServerInitializer(sslCtx));
	  
	              b.bind(PORT).sync().channel().closeFuture().sync();
	          } finally {
	              bossGroup.shutdownGracefully();
	              workerGroup.shutdownGracefully();
			 }
	}
}
