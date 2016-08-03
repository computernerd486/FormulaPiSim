import java.util.ArrayList;

public class Track {
	
	static final double ninety = Math.toRadians(90d);
	static final double twoPi = 2 * Math.PI;

	//Public variable for drawing
	public TrackNode[] nodes;
	public Float wallHeight;

	//Private for internal processesing
	String filename;
	Float radius;
	
	public Track(){}
	
	public void load(String file){
		
		ArrayList<Point2D> points = new ArrayList<>();
		//Read radius and wall height
		
		
		//read nodes
		
		
		//Do math generation
		nodes = new TrackNode[points.size()];
		for (int i = 0; i < points.size(); i++) {
			Point2D p0 = points.get((i == 0) ? points.size() - 1 : i - 1);
			Point2D p1 = points.get(i);
			Point2D p2 = points.get((i == points.size() - 1) ? 0 : i + 1);

			double t_ab = getSplitTheta(p0, p1, p2);
			//double t_ab = getSplitTheta(null, p1, p2);
			nodes[i] = new TrackNode(offsetPoint(p1, radius, t_ab), p1, offsetPoint(p1, -radius, t_ab));

			// a = offsetPoint(p1, distance, t_ab);
			// b = offsetPoint(p1, -distance, t_ab);
		}
	}
	
	/**
	 * Takes and averages the angle between 3 points to make an equal split
	 * 
	 * @param pA Previous Point
	 * @param pB Origin
	 * @param pC Next Point
	 * @return
	 */
	private static double getSplitTheta(Point2D pA, Point2D pB, Point2D pC) {
		Double t1, t2;
		// t1 = pA -> pB
		// t2 = pB -> pC
		// pB is centerpoint reference

		//System.out.println(pA + " " + pB + " " + pC);
		
		t1 = (pA != null) ? (t1 = Math.atan2(pA.y - pB.y, pA.x - pB.x)) : null;
		t2 = (pC != null) ? (t2 = Math.atan2(pC.y - pB.y, pC.x - pB.x)) : null;

		if (t1 != null && t2 != null) {
			double delta = ((t2 < 0 ? twoPi + t2 : t2) - t1) / 2;
			return t1 + ((delta < 0) ? Math.PI + delta : delta);			
		}

		return (t1 != null) ? t1  + ninety: (t2 != null) ? t2 + ninety : 0d;
	}
	
	/**
	 * Gets the point at the distance and angle from the selected point
	 * 
	 * @param p Origin to calculate offset from
	 * @param distance Distance from p
	 * @param theta Angle in Radians
	 * @return
	 */
	private static Point2D offsetPoint(Point2D p, double distance, double theta) {
		Point2D p2;

		p2 = new Point2D(
				Math.round(distance * (Math.cos(theta)) * 100d) / 100d + p.x,
				Math.round(distance * (Math.sin(theta)) * 100d) / 100d + p.y);

		return p2;
	}
}
