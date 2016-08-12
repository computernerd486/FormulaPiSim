package sim.object;

import sim.util.*;

public class Bot {

	//Remember all dimensions are 1cm = 1unit
	
	public Point2D dimensions;
	public Point2D position;
	public Point3D focus;
	public float height;
	
	public float direction;
	public float speed;
	
	public float p_m1, p_m2;
	
	public float tyre_diameter = 8.4f;
	public float tyre_radius = tyre_diameter / 2f;
	public float tyre_circ = (float) (Math.PI * tyre_diameter);
	
	public float m_ref_volt = 6f;
	public float m_run_volt = 8.2f;
	
	public float m_ref_rpm = 180f;
	public float m_run_rpm = (m_run_volt / m_ref_volt) * m_ref_rpm;
	
	//@see BotUpdater for this, in ms
	public float update_period = 20f;
	public float m_dist_peroid = tyre_circ * (m_run_rpm / (60f * 1000f) * update_period);
		
	public float bot_width = 8.4f;
	public float tyre_width = 4.2f;
	public float bot_radius = (bot_width / 2) + (tyre_width / 2); 
	
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
		this.dimensions = new Point2D(19, 18);
		this.position = start;
		this.height = 4.15f;
		this.speed = 2f;
		this.p_m1 = 0f;
		this.p_m2 = 0f;
		
		setDirection(angle);
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
		float velocityR = m_dist_peroid * p_m2;
		float velocityL = m_dist_peroid * p_m1;
		
		float angle = (float) Math.toRadians(direction);
		angle += (velocityR - velocityL) / (bot_radius * 2);
		position.x += 0.5 * (velocityR + velocityL) * Math.cos(angle);
		position.y += 0.5 * (velocityR + velocityL) * Math.sin(angle);
		//System.out.println(velocityL + " : " + velocityR);
		//System.out.println(angle + " : [" + position.x + "," + position.y + "]");
		
		setDirection((float) Math.toDegrees(angle));		
		
	}
	
	public void loadConfig(String file){
		//TODO: Load from config file
	}
}
