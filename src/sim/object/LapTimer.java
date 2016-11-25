package sim.object;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Timer;

import javax.swing.text.DateFormatter;

import sim.util.Point2D;

public class LapTimer {
	static final int MAX_KEPT = 5;
	
	Bot bot;
	Track track;
	
	Point2D lastPos;
	boolean lastPass;
	
	double s_x, s_y1, s_y2;
	
	public ArrayList<Lap> laps;
	public Lap currentLap;
	
	public LapTimer(Track track, Bot bot) {
		this.bot = bot;
		this.track = track;
		this.lastPass = false;
		
		s_x = track.startX;
		s_y1 = track.nodes[0].b.y;
		s_y2 = track.nodes[0].a.y;
		
		laps = new ArrayList<>();
	}
	
	enum Side {NA, LEFT, RIGHT}
	Side side =  Side.RIGHT;
	
	final float triggerWidth = 1f;
	public void check() {
		Side currentSide;
		Side prevSide = side;
		
		Point2D pos = bot.position;
		
		currentSide = (pos.x < s_x) ? Side.LEFT : Side.RIGHT;
		
		if (prevSide == Side.RIGHT && currentSide == Side.LEFT) {
			if (currentLap != null) {
				currentLap.stop();
				laps.add(currentLap);
				
				if (laps.size() > MAX_KEPT) {
					laps.remove(0);
				}
					
			} 
			
			currentLap = new Lap();
			currentLap.start();
			bot.tracker.newLap();

		}
		
		lastPos = bot.position;	
		side = currentSide;
	}
	
	public class Lap {		
		private final String format = "%02d:%02d.%03d";
		
		public long start;
		public long end = 0;
		
		public long length;
		
		public void start(){
			start = Calendar.getInstance().getTimeInMillis();
		}
		
		public void stop(){
			end = Calendar.getInstance().getTimeInMillis();
			length = end - start;
		}
		
		public long laptime() {
			
			if (end <= 0) {
				return (Calendar.getInstance().getTimeInMillis() - start);
			}
			
			return length;
		}
		
		@Override
		public String toString() {
			long time = laptime();
			
			long mili = time % 1000;
			long seconds = (time / 1000) % 60;
			long min = (time / 1000) / 60;
			
			
			return String.format(format, min, seconds, mili);
		}
	}
}
