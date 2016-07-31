
public class Bot {

	public Point2D position;
	public Point2D focus;
	public float height;
	
	public float direction;
	public float speed;
	
	float p_m1, p_m2;
	
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
		this.position = start;
		this.height = .3f;
		this.speed = .1f;
		this.p_m1 = 0f;
		this.p_m2 = 0f;
		
		setDirection(angle);
	}
	
	public void setDirection(float angle)
	{
		this.direction = angle; //(float) Math.toRadians(angle);
		this.focus = new Point2D(
				Math.cos(Math.toRadians(angle)) + position.x,
				Math.sin(Math.toRadians(angle)) + position.y);
		
		//System.out.println("Positon " + position);
		//System.out.println("Angle   " + direction);
		//System.out.println("Focus   " + focus);
	}
	
	/**
	 * This will need to be updated to take and chagne the angle depending
	 * on the motor percentages sent
	 * @param p_m1
	 * @param p_m2
	 */
	public void move()
	{
		//System.out.println("Movement Update: [" + p_m1 + ", " + p_m2 + "]");
		//currently use the angle and assume its turned/one input
		
		float angle = direction;
		//create movement vector
		double x = Math.cos(Math.toRadians(angle)) * (p_m1 * speed);
		double y = Math.sin(Math.toRadians(angle)) * (p_m1 * speed);
		
		position.x += x;
		position.y += y;
		
		setDirection(direction);
		
		
	}
}
