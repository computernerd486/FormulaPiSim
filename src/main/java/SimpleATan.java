/**
* Copyright 2016 Tim Pearce
**/

import sim.util.Point2D;

public class SimpleATan {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double d = 1d;
		
		Point2D p1 = new Point2D(0, 0);
		Point2D p2 = new Point2D(0, -1);
		double theta = Math.atan2(p2.y - p1.y, p2.x - p1.x) + Math.toRadians(90d);
		Point2D pE = new Point2D(
				Math.round(d * (Math.cos(theta)) * 100d) / 100d,
				Math.round(d * (Math.sin(theta)) * 100d) / 100d);

		
		System.out.println();
		System.out.println(pE);
		
		System.out.println();
		for (int i = 0; i <= 360; i+=5)
		{
			System.out.println(new Point2D(
					Math.cos(Math.toRadians(i)),
					Math.sin(Math.toRadians(i))));
		}
	}

}
