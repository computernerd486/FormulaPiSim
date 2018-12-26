/**
* Copyright 2016 Tim Pearce
**/

package sim.object;

import sim.util.Point2D;

public class TrackNode {
	public Point2D a, p, b;
	public Point2D[] lane; 
	
	public TrackNode(Point2D a, Point2D p, Point2D b)
	{
		this.a = a;
		this.p = p;
		this.b = b;
	}
}
