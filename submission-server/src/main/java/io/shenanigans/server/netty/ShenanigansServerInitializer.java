package io.shenanigans.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class ShenanigansServerInitializer extends ChannelInitializer<SocketChannel> {

	private SslContext m_sslContext;
	
	
	public ShenanigansServerInitializer(SslContext sslContext) {
		super();
		m_sslContext = sslContext;
	}


	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		 ChannelPipeline p = channel.pipeline();
		 p.addLast(m_sslContext.newHandler(channel.alloc()));
		 p.addLast(new HttpServerCodec());
		 p.addLast(new ShenanigansServerHandler());
		 //p.addLast(new HttpContentCompressor());
	}

}
