package sim.util.io.stream;


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.rtsp.RtspHeaderNames;
import io.netty.handler.codec.rtsp.RtspResponseStatuses;
import io.netty.handler.codec.rtsp.RtspVersions;
import io.netty.util.ReferenceCountUtil;
import sim.object.Bot;

/**
 * This is going to be an odd class, it'll kick off an http/rtsp server
 * and start streaming the video to that.
 * 
 * @author Tim Pearce
 *
 */
public class RTSPStreamer extends VideoStreamer {

	public boolean isRunning = false;
	public int port;
	
	ServerBootstrap b;
	EventLoopGroup workerGroup;
	
	BufferedImage out;
	
	public Bot bot;
	
	
	public RTSPStreamer() {
		super();
	}
	
	public RTSPStreamer(int fps) {
		super(fps);
	}
	
	public RTSPStreamer(Dimension size) {
		super(size);
	}
	
	public RTSPStreamer(int fps, Dimension size) {
		super(fps, size);
	}
	
	public void setupRTSPStreamer(Dimension size, int port) throws Exception
	{
		if (!isRunning) {
			this.out = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			this.port = port;
			this.size = size;
		} else {
			throw new Exception("Unable to setup Stream while Streaming is Active");
		}
	}
	
	@Override
	public void stream() {
		if (video != null)
		{
			out.getGraphics().drawImage(video, 0, 0,  size.width, size.height, null);
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		
		if (isRunning) {
			workerGroup.shutdownGracefully();
			isRunning = false;
		}
	};
	
	@Override
	public void run() {
		super.run();
		
		new Thread() {
			public void run() {
				
				System.out.println("Starting Server");
		        workerGroup = new NioEventLoopGroup();
		        try {
		            b = new ServerBootstrap(); // (2)
		            b.group(workerGroup);
		            b.channel(NioServerSocketChannel.class); // (3)
		            b.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
		                 @Override
		                 public void initChannel(SocketChannel ch) throws Exception {
		                	 ChannelPipeline pipe = ch.pipeline();
		                	 pipe.addLast("decoder", new HttpRequestDecoder());
		                	 pipe.addLast("encoder", new HttpResponseEncoder());
		                	 pipe.addLast("handler", new ResponseHandler());
		                	 pipe.addLast("codec", new HttpServerCodec());
		                 }
		             });
		            
		             b.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
		             b.childOption(ChannelOption.TCP_NODELAY, true);
		
		            // Bind and start to accept incoming connections.
		            ChannelFuture f = b.bind(port).sync(); // (7)
		
		            System.out.println("After Bind");
		            isRunning = true;
		            // Wait until the server socket is closed.
		            // In this example, this does not happen, but you can do that to gracefully
		            // shut down your server.
		            
		            f.channel().closeFuture().sync();
		        } catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					System.out.println("Shutting down server");
		            workerGroup.shutdownGracefully();
		        }
			}
		}.start();
	};


			
	
	class ResponseHandler extends ChannelInboundHandlerAdapter  {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			
			
			if (msg instanceof DefaultHttpRequest)
			{
				DefaultHttpRequest request = (DefaultHttpRequest)msg;
				String uri = request.uri();
				
				System.out.println("Client received: " + msg.toString()); //msg.toString(CharsetUtil.UTF_8));
				
				//Speed Updation from request call
				if (uri.contains("m1") && uri.contains("m2"))
				{
					QueryStringDecoder decoder = new QueryStringDecoder(uri);
					String m1 = decoder.parameters().get("m1").get(0);
					String m2 = decoder.parameters().get("m2").get(0);
					System.out.println(m1 + " : " + m2);
					
					try {
						Float m1_spd = null, m2_spd = null;
						
						if (m1 != null && !"".equals(m1))
							m1_spd = Math.max(-1, Math.min(1, Float.parseFloat(m1)));
						
						if (m1 != null && !"".equals(m1)) 
							m2_spd = Math.max(-1, Math.min(1, Float.parseFloat(m2)));
						
						if (m1_spd != null) bot.p_m1 = m1_spd;
						if (m2_spd != null) bot.p_m2 = m2_spd;
						
					} catch (Exception e) {
						System.err.println("Unable to parse speeds");
						e.printStackTrace(System.err);
					}
				}
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();   
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			
			ImageIO.write(out, "png", ios);
	        byte[] buffer = baos.toByteArray();
	        

	    	FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(buffer));
	    	httpResponse.headers().add(HttpHeaderNames.PRAGMA, "no-cache");
	        httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "image/png");
	        httpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, buffer.length);


			ctx.writeAndFlush(httpResponse);
			ios.close();
		}
				
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		
	}

}
