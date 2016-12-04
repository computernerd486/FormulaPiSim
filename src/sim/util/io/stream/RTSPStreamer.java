/**
* Copyright 2016 Tim Pearce
**/

package sim.util.io.stream;


import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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
import sim.object.Bot;

/**
 * This should be renamed HTTP Streamer, but anyway, it serves up an image of the first person view,
 * png format, for the request. Also in the request, it accepts speed for motor 1 and motor 2, 
 * adjusting the requested speed for the bot with those values.
 *
 */
public class RTSPStreamer extends VideoStreamer {

	public static String PROPERTIES_FILE = "settings/server.conf";
	
	public boolean isRunning = false;
	public int port;
	public boolean autoStart = false;
	
	ServerBootstrap b;
	EventLoopGroup workerGroup;
	
	BufferedImage out;
	Graphics2D g;
	
	ByteArrayOutputStream baos;
	ImageOutputStream ios;
	ImageWriter imageWriter;
	ImageWriteParam writerParams;
	
	byte[] buffer;
	ByteBuf httpBuffer;
	
	Timer updateProcess;
		
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
	
	public void loadProperties() {
		Properties props = new Properties();
		
		try {
			BufferedReader br = Files.newBufferedReader(Paths.get(PROPERTIES_FILE));
			props.load(br);
			br.close();
			
			System.out.println("Video Stream Settings:");
			for (Object key : props.keySet()) {
				System.out.println(key + " : " + props.getProperty((String) key));
			}
			
			System.out.println();
			
			String[] res = props.getProperty("res", "640x480").split("x");
			size = new Dimension(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
			port = Integer.parseInt(props.getProperty("port", "10000"));
			autoStart = Boolean.parseBoolean(props.getProperty("autostart"));
			
			
		} catch (Exception e) {
			System.err.println("Unable to read server settings file");
			e.printStackTrace(System.err);
		}
				
	}
	
	public void setupRTSPStreamer(Dimension size, int port) throws Exception
	{
		baos = new ByteArrayOutputStream();   
		ios = ImageIO.createImageOutputStream(baos);
		
		if (false) { //PNG
			imageWriter = ImageIO.getImageWritersByFormatName("PNG").next();
			writerParams = imageWriter.getDefaultWriteParam();;
		} else { //JPG
			
			imageWriter = ImageIO.getImageWritersByFormatName("JPG").next();
			JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
			jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpegParams.setCompressionQuality(.4f);
			
			writerParams = jpegParams;
		}
		
		
		imageWriter.setOutput(ios);			
		
		if (!isRunning) {
			this.out = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			this.port = port;
			this.size = size;
		
			g = (Graphics2D)out.getGraphics();
			
		} else {
			throw new Exception("Unable to setup Stream while Streaming is Active");
		}
	}
	
	@Override
	public void stream() {
		if (video != null)
		{
			g.drawImage(video, size.width, 0,  -size.width, size.height, null);
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		
		if (isRunning) {
			workerGroup.shutdownGracefully();
			updateProcess.cancel();
			
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
		
		updateProcess = new Timer(true);
		updateProcess.schedule(new UpdateBuffer(), 0, 30);
	};

	class UpdateBuffer extends TimerTask implements Runnable {
		@Override
		public void run() {
			try {
				baos.reset();
				imageWriter.write(null, new IIOImage(out, null, null), writerParams);
				buffer = baos.toByteArray();
				httpBuffer = Unpooled.wrappedBuffer(buffer);
				
			} catch (Exception e) {
				System.err.println("Unable to create image buffer");
				e.printStackTrace(System.err);
			}
		}
	}
	
	class ResponseHandler extends ChannelInboundHandlerAdapter  {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			super.channelRead(ctx, msg);
			
			if (msg instanceof DefaultHttpRequest)
			{
				DefaultHttpRequest request = (DefaultHttpRequest)msg;
				String uri = request.uri();
				
				//System.out.println("Client received: " + msg.toString()); //msg.toString(CharsetUtil.UTF_8));
				
				//Speed Updation from request call
				if (uri.contains("m1") && uri.contains("m2"))
				{
					QueryStringDecoder decoder = new QueryStringDecoder(uri);
					String m1 = decoder.parameters().get("m1").get(0);
					String m2 = decoder.parameters().get("m2").get(0);

					//System.out.println(m1 + " : " + m2);
					
					try {
						Float m1_spd = null, m2_spd = null;
						
						if (m1 != null && !"".equals(m1))
							m1_spd = Math.max(-1, Math.min(1, Float.parseFloat(m1)));
						
						if (m1 != null && !"".equals(m1)) 
							m2_spd = Math.max(-1, Math.min(1, Float.parseFloat(m2)));
						
						if (m1_spd != null) bot.p_m1 = m1_spd;
						if (m2_spd != null) bot.p_m2 = m2_spd;
						
					} catch (Exception e) {
						System.err.print("Unable to parse speeds: ");
						System.err.println(uri);
						//e.printStackTrace(System.err);
					}
				}
				
				if (uri.contains("l1"))
				{
					QueryStringDecoder decoder = new QueryStringDecoder(uri);
					String l1 = decoder.parameters().get("l1").get(0);
					
					if (l1 != null && !"".equals(l1))
						bot.light = "1".equals(l1);
				}
			}

		}
				
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			//long start = Calendar.getInstance().getTimeInMillis();
			
	    	FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, httpBuffer);
	    	httpResponse.headers().add(HttpHeaderNames.PRAGMA, "no-cache");
	        httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "image/png");
	        httpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, buffer);
	        
			ctx.writeAndFlush(httpResponse);
			ctx.close();
			
			//System.out.println(Calendar.getInstance().getTimeInMillis() - start);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}

		
	}

}
