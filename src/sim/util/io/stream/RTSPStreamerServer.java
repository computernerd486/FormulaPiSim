/**
* Copyright 2016 Tim Pearce
**/

package sim.util.io.stream;


import java.awt.Dimension;
import java.awt.image.BufferedImage;


import rtsp.*;




/**
 * This is going to be an odd class, it'll kick off an http/rtsp server
 * and start streaming the video to that.
 */
public class RTSPStreamerServer extends VideoStreamer {

	BufferedImage out;
	
	public RTSPStreamerServer() {
		super();
	}
	
	public RTSPStreamerServer(int fps) {
		super(fps);
	}
	
	public RTSPStreamerServer(Dimension size) {
		super(size);
	}
	
	public RTSPStreamerServer(int fps, Dimension size) {
		super(fps, size);
	}

	public void startServer()
	{
		out = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		
		new Thread() {
			public void run() {
				try {
					Server theServer = new Server();
			        theServer.runServer();
			        
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}

			}
		}.start();
	}
	
	@Override
	public void stream() {
		if (video != null)
		{
			out.getGraphics().drawImage(video, 0, 0,  size.width, size.height, null);
		}
	}

}
