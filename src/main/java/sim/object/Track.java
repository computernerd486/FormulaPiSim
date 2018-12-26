/**
* Copyright 2016 Tim Pearce
**/

package sim.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import sim.util.Point2D;

public class Track {
	
	static final double ninety = Math.toRadians(90d);
	static final double twoPi = 2 * Math.PI;

	//Public variable for drawing
	public IndicatorBar lights;
	public TrackNode[] nodes;
	public Float wallHeight;
	public Point2D bounds;
	public Point2D[] startPositions;
	public Float lightX;
	public Float startX;
	public int lanes;
	
	//Private for internal processesing
	String filename;
	Float radius;
	
	public Track(){
		nodes = new TrackNode[0];
		wallHeight = 0f;
		radius = 0f;
	}
	
	public void load(String file){
		
		ArrayList<Point2D> points = new ArrayList<>();
		
		Path p = Paths.get("tracks/", file);
		System.out.println("Loading File: [" + p.toUri() + "]");
		
		//Read the custom track file
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("tracks/", file))) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("#") || "".equals(line.trim())) continue;
				
				//Check if its the config/width/height line
				if (line.startsWith("WIDTH:")) {
					radius = Float.valueOf(line.substring(line.indexOf(':') + 1));
					continue;
				}
				
				if (line.startsWith("HEIGHT:")) {
					wallHeight = Float.valueOf(line.substring(line.indexOf(':') + 1));
					continue;
				}
				
				if (line.startsWith("BOUNDS:")) {
					String[] b = line.substring(line.indexOf(':') + 1).split(",");
					bounds = new Point2D(Double.valueOf(b[0]), Double.valueOf(b[1]));
					continue;
				}
				
				if (line.startsWith("START_LINE:")) {
					startX = Float.valueOf(line.substring(line.indexOf(':') + 1));
					continue;
				}
				
				if (line.startsWith("LIGHTS:")) {
					lightX = Float.valueOf(line.substring(line.indexOf(':') + 1));
					continue;
				}
				
				if (line.startsWith("LANES:")) {
					lanes = Integer.valueOf(line.substring(line.indexOf(':') + 1));
					continue;
				}
			
				if (line.matches("^[a-zA-Z](.*)"))
					continue;
				
				String[] coord = line.split(",");
				points.add(new Point2D(Double.valueOf(coord[0]), Double.valueOf(coord[1])));
				
			}
			
		} catch (Exception e) {
			System.err.println("Error loading file [" + file + "]:");
			System.err.println(e);
		} 
		
		float lane_width = (radius * 2f) / (lanes + 1f);
		
		//Do math generation
		nodes = new TrackNode[points.size()];
		
		for (int i = 0; i < points.size(); i++) {
			Point2D p0 = points.get((i == 0) ? points.size() - 1 : i - 1);
			Point2D p1 = points.get(i);
			Point2D p2 = points.get((i == points.size() - 1) ? 0 : i + 1);

			double t_ab = getSplitTheta(p0, p1, p2);
			nodes[i] = new TrackNode(offsetPoint(p1, radius, t_ab), p1, offsetPoint(p1, -radius, t_ab));
			
			int center = lanes / 2;
			Point2D[] lane = new Point2D[lanes];
			for (int l = 0; l < lanes; l++) {
				if (l == center) {
					lane[l] = p1;
					continue;
				}
				
				lane[l] = offsetPoint(p1, (lane_width * -(center - l)), t_ab);
			}
			nodes[i].lane = lane;
		}
		
		lights = new IndicatorBar(new Point2D(lightX, nodes[0].p.y), radius);
		startPositions = new Point2D[lanes];
		
		for (int l = 1; l <= lanes; l++) {
			startPositions[l - 1] = new Point2D(startX, nodes[0].b.y + (l * lane_width));
			System.out.println("Start [" + l + "] : " + startPositions[l - 1]);
		}
	}
	
	/**
	 * Saves the track out to a file
	 * @param file
	 */
	public void save(String file){
		
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("tracks/", file))) {
			
			System.out.println("Writing out file: [" + file + "]");
			
			writer.write("WIDTH:" + radius); 
			writer.newLine();
			writer.write("HEIGHT:" + wallHeight);
			writer.newLine();
			writer.write("BOUNDS:");
			writer.newLine();
			
			for (TrackNode node : nodes) {
				//System.out.println(node.p);
				writer.write(node.p.x + "," + node.p.y);
				writer.newLine();
			}
			
			writer.close();
		} catch (Exception e) {
			System.err.println("Error saving file:");
			System.err.println(e);
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
			return t1 + ((delta < 0) ? (Math.PI + delta) : (delta > Math.PI) ? (delta - Math.PI) : delta);			
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
