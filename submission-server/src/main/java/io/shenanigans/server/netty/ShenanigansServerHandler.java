package io.shenanigans.server.netty;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import io.netty.util.CharsetUtil;

import java.net.URI;

public class ShenanigansServerHandler extends SimpleChannelInboundHandler<Object>{

	private static final HttpDataFactory factory = new DefaultHttpDataFactory();
	private HttpPostRequestDecoder decoder;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest){
			HttpRequest request =  (HttpRequest) msg;
			System.out.println(request.getUri());
			URI uri = new URI(request.getUri());
			if (uri.getPath().equals("/submitFingerprint")){

				if (request.getMethod().equals(HttpMethod.POST)){
					try {
						decoder = new HttpPostRequestDecoder(factory, request);
					} catch (ErrorDataDecoderException e1) {
						e1.printStackTrace();
						// FIXME -log this properly
						ctx.channel().close();
						return;
					} catch (IncompatibleDataDecoderException e1) {
						// GET Method: should not try to create a HttpPostRequestDecoder
						// So OK but stop here
						e1.printStackTrace(); // FIXME
						return;
					}

				} else {
					writeResponse(ctx.channel(), "Not a post.");
				}
			} else {
				// TODO handle 404
			}
		}
		if (decoder != null){
			if (msg instanceof HttpContent){
				HttpContent chunk = (HttpContent) msg;
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e1) {
					e1.printStackTrace();
					ctx.channel().close();
					return;
				}
				if (chunk instanceof LastHttpContent) {
					writeResponse(ctx.channel(), "Finished!\n");

					reset(); // FIXME - shouldn't we reset on all exit conditions where connection is now invalid?
				}
			}
		}

	}

	private void writeResponse(Channel channel, String s){

		ByteBuf buf = copiedBuffer(s, CharsetUtil.UTF_8);

		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		ChannelFuture future = channel.writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);

	}


	private void reset() {

		decoder.destroy();
		decoder = null;
	}


}
