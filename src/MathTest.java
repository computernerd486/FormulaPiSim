/**
* Copyright 2016 Tim Pearce
**/

import java.text.DecimalFormat;
import java.util.ArrayList;

import sim.object.TrackNode;
import sim.util.Point2D;

public class MathTest {

	static final double ninety = Math.toRadians(90d);
	static final MathTest m = new MathTest();
	static final TrackNode[] track;
	static final double distance = 1.5d;

	static {
		ArrayList<Point2D> points = new ArrayList<>();
		
		
		points.add(new Point2D(15, 3));
		points.add(new Point2D(6, 3));
		points.add(new Point2D(5, 3));
		points.add(new Point2D(4, 4));
		points.add(new Point2D(3, 6));
		points.add(new Point2D(3, 8));
		points.add(new Point2D(10, 14));
		points.add(new Point2D(11, 14));
		points.add(new Point2D(15, 12));
		points.add(new Point2D(17, 12));
		points.add(new Point2D(19, 13));
		points.add(new Point2D(21, 16));
		points.add(new Point2D(23, 17));
		points.add(new Point2D(24, 17));
		points.add(new Point2D(26, 16));
		points.add(new Point2D(27, 14));
		points.add(new Point2D(27, 7));
		points.add(new Point2D(26, 5));
		points.add(new Point2D(25, 4));
		points.add(new Point2D(23, 3));
		points.add(new Point2D(22, 3));
		
		track = new TrackNode[points.size()];
		for (int i = 0; i < points.size(); i++) {
			Point2D p0 = points.get((i == 0) ? points.size() - 1 : i - 1);
			Point2D p1 = points.get(i);
			Point2D p2 = points.get((i == points.size() - 1) ? 0 : i + 1);

			double t_ab = getSplitTheta(p0, p1, p2);
			//double t_ab = getSplitTheta(null, p1, p2);
			track[i] = new TrackNode(offsetPoint(p1, distance, t_ab), p1, offsetPoint(p1, -distance, t_ab));

			// a = offsetPoint(p1, distance, t_ab);
			// b = offsetPoint(p1, -distance, t_ab);
		}
		
		System.out.println("-----Nodes-----");
		for (TrackNode n : track) {
			System.out.println(n.a + " " + n.p + " " + n.b);
		}
		System.out.println("---------------");

	}

	public static void main(String[] args) {

		int steps = 5;
		double distance = steps;

		Point2D p0, p1, p2;
		p0 = new Point2D(-5, 0);
		p1 = new Point2D(0, 0);
		p2 = new Point2D(5, 5);

		System.out.println("P0 " + p0);
		System.out.println("P1 " + p1);
		System.out.println("P2 " + p2);
		System.out.println();

		Point2D a, b;
		double t_ab = getSplitTheta(p0, p1, p2);

		a = offsetPoint(p1, distance, t_ab);
		b = offsetPoint(p1, -distance, t_ab);

		System.out.println();
		System.out.println(a);
		System.out.println(b);
	}

	/**
	 * Takes and averages the angle between 3 points to make an equal split
	 * 
	 * @param pA
	 * @param pB
	 * @param pC
	 * @return
	 */
	private static double getSplitTheta(Point2D pA, Point2D pB, Point2D pC) {
		Double t1, t2;
		// t1 = pA -> pB
		// t2 = pB -> pC
		// pB is centerpoint reference
		System.out.println();
		System.out.println(pA + " " + pB + " " + pC);
		
		t1 = (pA != null) ? (t1 = Math.atan2(pA.y - pB.y, pA.x - pB.x)) : null;
		t2 = (pC != null) ? (t2 = Math.atan2(pC.y - pB.y, pC.x - pB.x)) : null;

		if (t1 != null && t2 != null) {
			double delta = ((t2 < 0 ? (2 * Math.PI) + t2 : t2) - t1) / 2;
			return t1 + ((delta < 0) ? Math.PI + delta : delta);			
		}

		return (t1 != null) ? t1  + ninety: (t2 != null) ? t2 + ninety : 0d;
	}

	/**
	 * Gets the point at the distance and angle from the selected point
	 * 
	 * @param p
	 * @param distance
	 * @param theta
	 * @return
	 */
	private static Point2D offsetPoint(Point2D p, double distance, double theta) {
		Point2D p2;
		// System.out.println(Math.toDegrees(theta) + " @ " + distance + " " +
		// p);

		p2 = new Point2D(
				Math.round(distance * (Math.cos(theta)) * 100d) / 100d + p.x,
				Math.round(distance * (Math.sin(theta)) * 100d) / 100d + p.y);

		return p2;
	}

}