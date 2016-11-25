/**
* Copyright 2016 Tim Pearce
**/

package sim.object;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.prefs.Preferences;

import sim.util.*;

public class Bot {

	//Remember all dimensions are 1cm = 1unit
	
	//I know i'll get flack about public variables, its a bad idea for enterprise software
	//but this is being done for speed, stack push/pop takes cpu cycles which would be better
	//suited to keeping the framerate up
	
	public static String PROPERTIES_FILE = "settings/bot.conf";
	
	public Point2D dimensions;
	public Point2D position;
	public Point3D focus;
	public float height;
	
	public float direction;
	
	public float p_m1, p_m2;
	
	public float tyre_diameter = 8.4f;
	public float tyre_radius = tyre_diameter / 2f;
	public float tyre_circ = (float) (Math.PI * tyre_diameter);
	
	public float m_ref_volt = 6f;
	public float m_run_volt = 6.7f;
	
	public float m_ref_rpm = 180f;
	public float m_run_rpm = (m_run_volt / m_ref_volt) * m_ref_rpm;
	
	//@see BotUpdater for this, in ms
	public float update_period = 20f;
	public float m_dist_peroid = tyre_circ * (m_run_rpm / (60f * 1000f) * update_period);
		
	public float bot_width = 8.4f;
	public float tyre_width = 4.2f;
	public float bot_radius = (bot_width / 2) + (tyre_width / 2); 
	
	public float bot_bound_width = 18f;
	public float bot_bound_length = 19f;
	
	public Motor m1, m2;
	public boolean light;
	
	public LapTracker tracker;
	public LapTimer laptimer;
	
	public Bot()
	{
		this(new Point2D(0,0));
	}
	
	public Bot(Point2D start)
	{
		this(start, 0f);
	}
	
	public Bot(Point2D start, float angle)
	{
		//I'm actually pointing left
		this.dimensions = new Point2D(bot_bound_length, bot_bound_width);
		this.position = start;
		this.height = 4.15f;
		this.p_m1 = 0f;
		this.p_m2 = 0f;
		this.light = false;
		
		m1 = new Motor();
		m2 = new Motor();
		tracker = new LapTracker();
		
		setDirection(angle);
		
		loadProperties();
	}
	
	private void loadProperties() {
		Properties props = new Properties();
		
		try {
			BufferedReader br = Files.newBufferedReader(Paths.get(PROPERTIES_FILE));
			props.load(br);
			br.close();
			
			System.out.println("Bot Settings:");
			for (Object key : props.keySet()) {
				System.out.println(key + " : " + props.getProperty((String) key));
			}
			System.out.println();
					
			tyre_diameter 	= Float.parseFloat(props.getProperty("tyre_diameter", "8.4"));
			m_ref_volt 		= Float.parseFloat(props.getProperty("motor_ref_volt", "6"));
			m_run_volt 		= Float.parseFloat(props.getProperty("motor_max_volt", "6.7"));
			m_ref_rpm 		= Float.parseFloat(props.getProperty("motor_ref_rpm", "180"));
			bot_width 		= Float.parseFloat(props.getProperty("body_width", "8.4"));
			tyre_width 		= Float.parseFloat(props.getProperty("tyre_width", "4.2"));
			
			bot_bound_width = Float.parseFloat(props.getProperty("bound_width", "18"));
			bot_bound_length = Float.parseFloat(props.getProperty("bount_length", "19"));
			
			m1.accel_rate = m2.accel_rate = Float.parseFloat(props.getProperty("motor_accel", "0.01"));
			m1.decel_rate = m2.decel_rate = Float.parseFloat(props.getProperty("motor_decel", "-0.002"));
			
			recalc();
			
		} catch ( Exception e) {
			System.err.println("Unable to load properties for Bot");
		}
	}
	
	public void recalc() {

		tyre_radius = tyre_diameter / 2f;
		tyre_circ = (float) (Math.PI * tyre_diameter);

		m_run_rpm = (m_run_volt / m_ref_volt) * m_ref_rpm;
		m_dist_peroid = tyre_circ * (m_run_rpm / (60f * 1000f) * update_period);

		bot_radius = (bot_width / 2) + (tyre_width / 2); 	
	}
	
	/**
	 * This was for the inital so that i could test movement.
	 * Use the two motor style instead, just set p_m1 and p_m2 separate
	 * @param angle
	 */
	public void setDirection(float angle)
	{
		this.direction = angle; //(float) Math.toRadians(angle);
		this.focus = new Point3D(
				(Math.cos(Math.toRadians(angle)) * 10d) + position.x,
				(Math.sin(Math.toRadians(angle)) * 10d) + position.y,
				(double)(height - 0.2f)); //2% down angle
		
		//System.out.println("Positon " + position);
		//System.out.println("Angle   " + direction);
		//System.out.println("Focus   " + focus);
	}
	
	/**
	 * This 
	 * @param p_m1
	 * @param p_m2
	 */
	public void move()
	{		
		//m1 is left, m2 is right
		m1.spd_sig = p_m1;
		m2.spd_sig = p_m2;
		m1.tick();
		m2.tick();
		
		//System.out.println(m1 + " " + m2);
		
		float velocityR = m_dist_peroid * m2.spd_act;
		float velocityL = m_dist_peroid * m1.spd_act;
		
		float angle = (float) Math.toRadians(direction);
		angle += (velocityR - velocityL) / (bot_radius * 2);
		position.x += (0.5 * (velocityR + velocityL)) * Math.cos(angle);
		position.y += (0.5 * (velocityR + velocityL)) * Math.sin(angle);
		//System.out.println(velocityL + " : " + velocityR);
		//System.out.println(angle + " : [" + position.x + "," + position.y + "]");
		
		setDirection((float) Math.toDegrees(angle));		
		
		tracker.currentLap.add(new Point2D(position.x, position.y));
	}
	
	public void loadConfig(String file){
		//TODO: Load from config file
	}
}
