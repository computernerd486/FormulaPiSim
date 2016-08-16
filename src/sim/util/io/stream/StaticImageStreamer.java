/**
* Copyright 2016 Tim Pearce
**/

package sim.util.io.stream;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class StaticImageStreamer extends VideoStreamer {

	String file;
	BufferedImage out;
	
	public StaticImageStreamer() {
		super();
	}
	
	public StaticImageStreamer(int fps) {
		super(fps);
	}
	
	public StaticImageStreamer(Dimension size) {
		super(size);
	}
	
	public StaticImageStreamer(int fps, Dimension size) {
		super(fps, size);
	}
	
	public void setupStaticStreamer(String file) throws IOException
	{
		this.file = file;
		
		out = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
	}
	
	@Override
	public void stream() {

		if (video != null)
		{
			out.getGraphics().drawImage(video, 0, 0,  size.width, size.height, null);
			
			//new Thread() {
			//	public void run() {
					try {
						OutputStream os = Files.newOutputStream(Paths.get(file), StandardOpenOption.CREATE);
						ImageOutputStream ios = ImageIO.createImageOutputStream(os);
						ImageIO.write(out, "png", ios);
						os.close();
						
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
			//	}
			//}.start();
		}
	}

}
