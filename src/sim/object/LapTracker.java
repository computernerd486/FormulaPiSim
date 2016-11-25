package sim.object;

import java.util.LinkedList;

import sim.util.Point2D;

public class LapTracker {

	public LinkedList<Point2D> currentLap;
	public LinkedList<Point2D> lastLap;
	
	public LapTracker () {
		currentLap = new LinkedList<>();
		lastLap = new LinkedList<>();
	}
	
	public void newLap() {
		lastLap = currentLap;
		currentLap = new LinkedList<>();
	}
}
