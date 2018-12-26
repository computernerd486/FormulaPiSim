/**
* Copyright 2016 Tim Pearce
**/

package sim.util.io.stream;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public abstract class VideoStreamer implements Runnable{
	
	//This is the video object that will be streamed.
	public BufferedImage video;
	
	protected Timer ticker;
	protected int period;
	public Dimension size;
	
	
	public VideoStreamer() { 
		this(60, new Dimension(640,480));
	}
	
	public VideoStreamer(int fps) {
		this(fps, new Dimension(640,480));
	}
	
	public VideoStreamer(Dimension size) 
	{
		this(60, size);
	}
	
	public VideoStreamer(int fps, Dimension size) {
		this.period = 1000 / fps;
		this.size = size;
		this.ticker = new Timer(true);
	}
	
	@Override
	public void run() {
		this.ticker.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				stream();
			}
		}, 0, period);
	}
	
	public void stop() {
		this.ticker.cancel();
		this.ticker = new Timer(true);
	}
	
	
	public abstract void stream();
}
